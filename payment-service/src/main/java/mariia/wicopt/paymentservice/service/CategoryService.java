package mariia.wicopt.paymentservice.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import mariia.wicopt.paymentservice.infrastructure.entities.Category;
import mariia.wicopt.paymentservice.infrastructure.repository.CategoryRepository;
import mariia.wicopt.paymentservice.presentation.dto.request.CategoryCreateRequestDto;
import mariia.wicopt.paymentservice.presentation.dto.request.CategoryUpdateRequestDto;
import mariia.wicopt.paymentservice.presentation.dto.response.CategoryResponseDto;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponseDto> getAll(String groupId) {
        return categoryRepository.findAvailableForGroup(groupId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public CategoryResponseDto getById(Long id) {
        return categoryRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
    }

    @Transactional
    public CategoryResponseDto create(CategoryCreateRequestDto request) {
        Category category = new Category();
        category.setCategoryName(request.getCategoryName());
        category.setGroupId(request.getGroupId());
        category.setShared(request.isShared());
        return toDto(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponseDto update(Long id, CategoryUpdateRequestDto request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
        category.setCategoryName(request.getCategoryName());
        category.setShared(request.isShared());
        return toDto(categoryRepository.save(category));
    }


    public List<CategoryResponseDto> getByShared(String groupId, boolean isShared) {
        return categoryRepository.findAllByGroupIdAndIsShared(groupId, isShared)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }

    private CategoryResponseDto toDto(Category c) {
        CategoryResponseDto dto = new CategoryResponseDto();
        dto.setCategoryId(c.getCategoryId());
        dto.setCategoryName(c.getCategoryName());
        dto.setGroupId(c.getGroupId());
        dto.setShared(c.isShared());
        return dto;
    }
}