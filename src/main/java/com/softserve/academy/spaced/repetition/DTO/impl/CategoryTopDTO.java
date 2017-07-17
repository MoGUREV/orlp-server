package com.softserve.academy.spaced.repetition.DTO.impl;

import com.softserve.academy.spaced.repetition.DTO.DTO;
import com.softserve.academy.spaced.repetition.domain.Category;
import org.springframework.hateoas.Link;

public class CategoryTopDTO extends DTO<Category> {
    public CategoryTopDTO(Category category, Link parentLink) {
        super(category, parentLink);
    }

    public String getName() {
        return getEntity().getName();
    }

    public String getImagebase64() {
        return getEntity().getImagebase64();
    }
}