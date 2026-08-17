package mariia.wicopt.paymentservice.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import mariia.wicopt.paymentservice.presentation.dto.request.CategoryCreateRequestDto;
import mariia.wicopt.paymentservice.presentation.dto.request.CategoryUpdateRequestDto;
import mariia.wicopt.paymentservice.presentation.dto.response.CategoryResponseDto;
import mariia.wicopt.paymentservice.service.CategoryService;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryResponseDto> getAll(@RequestParam String groupId) {
        return categoryService.getAll(groupId);
    }

    @GetMapping("/filter")
    public List<CategoryResponseDto> getByShared(
            @RequestParam String groupId,
            @RequestParam boolean isShared) {
        return categoryService.getByShared(groupId, isShared);
    }

    @PostMapping
    public CategoryResponseDto create(@RequestBody CategoryCreateRequestDto request) {
        return categoryService.create(request);
    }

    @PutMapping("/{id}")
    public CategoryResponseDto update(
            @PathVariable Long id,
            @RequestBody CategoryUpdateRequestDto request) {
        return categoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}