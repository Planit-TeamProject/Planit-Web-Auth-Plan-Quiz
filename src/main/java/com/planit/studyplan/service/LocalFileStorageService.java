package com.planit.studyplan.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.planit.global.exception.BusinessException;
import com.planit.global.exception.ErrorCode;

@Service
public class LocalFileStorageService implements FileStorageService {

	// REQ-B-002: 목차 파일은 PDF, JPG, PNG 만 허용한다.
	private static final List<String> ALLOWED_EXTENSIONS = List.of("pdf", "jpg", "jpeg", "png");

	// REQ-NF-017: 목차 업로드 파일은 최대 10MB.
	// application.yml 의 spring.servlet.multipart.max-file-size 가 1차 방어선이지만,
	// 그 설정값이 바뀌어도 이 도메인 규칙은 항상 지켜지도록 여기서도 한 번 더 검사한다.
	private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;

	@Value("${planit.file.upload-dir:./uploads/toc}")
	private String uploadDir;

	@Override
	public String store(MultipartFile file) {
		if (file.getSize() > MAX_FILE_SIZE_BYTES) {
			throw new BusinessException(ErrorCode.STUDY_PLAN_TOC_FILE_TOO_LARGE);
		}

		String originalFilename = file.getOriginalFilename();
		String extension = extractExtension(originalFilename);

		if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {
			throw new BusinessException(ErrorCode.STUDY_PLAN_TOC_FILE_TYPE_INVALID);
		}

		try {
			Path dir = Path.of(uploadDir);
			Files.createDirectories(dir);

			String storedFilename = UUID.randomUUID() + "." + extension;
			Path target = dir.resolve(storedFilename);
			file.transferTo(target);

			return target.toString();
		} catch (IOException e) {
			throw new UncheckedIOException("목차 파일 저장에 실패했습니다: " + originalFilename, e);
		}
	}

	private String extractExtension(String filename) {
		if (filename == null || !filename.contains(".")) {
			throw new BusinessException(ErrorCode.STUDY_PLAN_TOC_FILE_TYPE_INVALID);
		}
		return filename.substring(filename.lastIndexOf('.') + 1);
	}
}
