package com.liquidacionremates.app.controller;

import com.liquidacionremates.app.Service.ClientService;
import com.liquidacionremates.app.Service.ProductService;
import com.liquidacionremates.app.dto.ProductDTO;
import com.liquidacionremates.app.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ClientService clientService;

    @GetMapping
    public String listProducts(Model model) {
        List<ProductDTO> products = productService.findAll();

        model.addAttribute("products", products);

        return "products/list";
    }

    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable Long id, @ModelAttribute ProductDTO productDTO) {
        productService.update(id, productDTO);
        return "redirect:/products";

    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return "redirect:/products";
    }

    @PostMapping("/mark-sold/{id}")
    public String markAsSold(@PathVariable Long id) {
        productService.markAsSold(id);
        return "redirect:/products";
    }

    @PostMapping("/mark-unsold/{id}")
    public String markAsUnsold(@PathVariable Long id) {
        productService.markAsUnsold(id);
        return "redirect:/products";
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("product", new ProductDTO());

        model.addAttribute("clients", clientService.findAll());
        return "products/form";
    }

    @PostMapping("/save")
    public String saveProduct(@ModelAttribute("product") ProductDTO productDTO) {
            productService.save(productDTO);
            return "redirect:/products";
    }

    @PostMapping("/update-sale/{id}")
    public String updateSale(@PathVariable Long id,
                             @RequestParam BigDecimal salePrice,
                             @RequestParam String status,
                             @RequestParam(required = false) Long buyerId,
                             @RequestParam Long auctionId) {
        productService.updateSaleData(id, salePrice, status, buyerId);
        return "redirect:/auctions/" + auctionId + "/catalog";
    }

    @GetMapping("/search-ajax")
    @ResponseBody
    public List<ProductDTO> searchProductsAjax(@RequestParam("q") String query) {
        return productService.searchByNameList(query);
    }

}