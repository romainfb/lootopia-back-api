package com.lootopia.lootopia_app.infrastructure.in.web;

import com.lootopia.lootopia_app.application.port.in.CreateArtefactUseCase;
import com.lootopia.lootopia_app.application.port.out.FileStoragePort;
import com.lootopia.lootopia_app.domain.ArtifactRarity;
import com.lootopia.lootopia_app.domain.model.Artifact;
import com.lootopia.lootopia_app.infrastructure.in.web.dto.ArtifactWebFormRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/artefacts")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.admin.ui.enabled", havingValue = "true")
public class ArtefactAdminWebController {

    private final CreateArtefactUseCase createArtefactUseCase;
    private final FileStoragePort fileStoragePort;

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("artifactRequest", new ArtifactWebFormRequest());
        model.addAttribute("rarities", ArtifactRarity.values());
        return "admin/artefacts/create";
    }

    @PostMapping("/create")
    public String createArtefact(
            @Valid @ModelAttribute("artifactRequest") ArtifactWebFormRequest request,
            BindingResult bindingResult,
            @RequestParam("image") MultipartFile image,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (image==null || image.isEmpty()) {
            bindingResult.rejectValue(null, "image.required", "Un fichier .glb est requis");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("rarities", ArtifactRarity.values());
            return "admin/artefacts/create";
        }

        try {
            String originalFileName = image.getOriginalFilename()!=null ? image.getOriginalFilename().replace(" ", "_"):"";
            String uniqueFileName = fileStoragePort.uploadFile(image, originalFileName);
            // Store only the unique file name in the database, not the full URL
            // The full URL will be generated dynamically when needed using fileStoragePort.generateUrl(uniqueFileName)

            Artifact artifact = Artifact.builder()
                    .title(request.getTitle())
                    .rarity(request.getRarity())
                    .description(request.getDescription())
                    .imageUrl(uniqueFileName) // Store only the unique file name
                    .cacheId(request.getCacheId())
                    .userId(request.getUserId())
                    .build();

            createArtefactUseCase.createArtefact(artifact);
            redirectAttributes.addFlashAttribute("successMessage", "Artefact créé avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la création : " + e.getMessage());
        }

        return "redirect:/admin/artefacts/create";
    }
}
