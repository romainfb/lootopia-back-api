package com.lootopia.lootopia_app.infrastructure.in.rest.dto;

import com.lootopia.lootopia_app.application.validation.ValidEmail;
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

    @ValidEmail(nullable = true)
    private String email;

    @Nullable
    private String firstName;

    @Nullable
    private String lastName;

    @Nullable
    private String username;

    @Nullable
    private MultipartFile profileImage;
}
