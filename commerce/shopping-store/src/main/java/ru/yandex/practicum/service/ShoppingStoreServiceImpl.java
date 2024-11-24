package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.exeption.ProductNotFoundException;
import ru.yandex.practicum.mapper.ProductMapper;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.repository.ProductRepository;
import ru.yandex.practicum.shoppingStore.dto.PageableDto;
import ru.yandex.practicum.shoppingStore.dto.ProductDto;
import ru.yandex.practicum.shoppingStore.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.shoppingStore.enums.ProductCategory;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShoppingStoreServiceImpl implements ShoppingStoreService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public List<ProductDto> getProductsByCategory(ProductCategory category, PageableDto pageableDto) {
        log.info("Получение продуктов категории: {} с параметрами пейджинга: {}", category, pageableDto);
        Pageable pageable = convertToPageable(pageableDto);
        List<Product> products = productRepository.findAllByProductCategory(category, pageable).getContent();
        log.info("Найдено {} продуктов категории: {}", products.size(), category);
        return products.stream()
                .map(productMapper::toProductDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductDto createNewProduct(ProductDto productDto) {
        log.info("Создание нового продукта: {}", productDto);
        Product product = productMapper.toProduct(productDto);
        Product savedProduct = productRepository.save(product);
        log.info("Продукт успешно создан с ID: {}", savedProduct.getProductId());
        return productMapper.toProductDto(savedProduct);
    }

    @Override
    @Transactional
    public ProductDto updateProduct(ProductDto productDto) {
        UUID productId = productDto.getProductId();
        log.info("Обновление продукта с ID: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("Продукт с ID {} не найден", productId);
                    return new ProductNotFoundException("Продукт не найден");
                });

        updateProductFields(product, productDto);

        Product updatedProduct = productRepository.save(product);
        log.info("Продукт с ID {} успешно обновлен", updatedProduct.getProductId());
        return productMapper.toProductDto(updatedProduct);
    }

    @Override
    @Transactional
    public boolean removeProductFromStore(UUID productId) {
        log.info("Удаление продукта с ID: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("Продукт с ID {} не найден", productId);
                    return new ProductNotFoundException("Продукт не найден");
                });

        productRepository.delete(product);
        log.info("Продукт с ID {} успешно удален", productId);
        return true;
    }

    @Override
    @Transactional
    public boolean setProductQuantityState(SetProductQuantityStateRequest request) {
        UUID productId = request.getProductId();
        log.info("Обновление состояния количества для продукта с ID: {}, новое состояние: {}", productId, request.getQuantityState());
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("Продукт с ID {} не найден", productId);
                    return new ProductNotFoundException("Продукт не найден");
                });

        product.setQuantityState(request.getQuantityState());
        productRepository.save(product);
        log.info("Состояние количества для продукта с ID {} успешно обновлено", productId);
        return true;
    }

    @Override
    public ProductDto getProduct(UUID productId) {
        log.info("Получение продукта с ID: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("Продукт с ID {} не найден", productId);
                    return new ProductNotFoundException("Продукт не найден");
                });

        log.info("Продукт с ID {} успешно найден", productId);
        return productMapper.toProductDto(product);
    }

    private void updateProductFields(Product product, ProductDto productDto) {
        if (productDto.getProductName() != null) {
            product.setProductName(productDto.getProductName());
        }
        if (productDto.getDescription() != null) {
            product.setDescription(productDto.getDescription());
        }
        if (productDto.getImageSrc() != null) {
            product.setImageSrc(productDto.getImageSrc());
        }
        if (productDto.getQuantityState() != null) {
            product.setQuantityState(productDto.getQuantityState());
        }
        if (productDto.getProductState() != null) {
            product.setProductState(productDto.getProductState());
        }
        if (productDto.getRating() > 0) {
            product.setRating(productDto.getRating());
        }
        if (productDto.getProductCategory() != null) {
            product.setProductCategory(productDto.getProductCategory());
        }
        if (productDto.getPrice() != null && productDto.getPrice().compareTo(BigDecimal.ONE) >= 0) {
            product.setPrice(productDto.getPrice());
        }
    }

    private Pageable convertToPageable(PageableDto pageableDto) {
        if (pageableDto.getSort() == null || pageableDto.getSort().isEmpty()) {
            return PageRequest.of(pageableDto.getPage(), pageableDto.getSize());
        }

        Sort sort = Sort.by(
                pageableDto.getSort().stream()
                        .map(sortStr -> {
                            String[] sortParams = sortStr.split(",");
                            if (sortParams.length == 2 && sortParams[1].equalsIgnoreCase("desc")) {
                                return Sort.Order.desc(sortParams[0]);
                            }
                            return Sort.Order.asc(sortParams[0]);
                        })
                        .toList()
        );

        return PageRequest.of(pageableDto.getPage(), pageableDto.getSize(), sort);
    }
}
