package com.lootopia.lootopia_app.infrastructure.in.rest.dto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserToUpdateDto {

    @Nullable
    private String username;

    @Nullable
    private MultipartFile profileImage;
}
