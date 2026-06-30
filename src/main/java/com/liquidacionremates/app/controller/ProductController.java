package com.liquidacionremates.app.controller;

import com.liquidacionremates.app.Service.ClientService;
import com.liquidacionremates.app.Service.ProductService;
import com.liquidacionremates.app.dto.ProductDTO;
import com.liquidacionremates.app.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ClientService clientService;

    @GetMapping
    public String listProducts(
            @RequestParam(required = false) Long searchId,
            @RequestParam(required = false) String searchName,
            @RequestParam(required = false) Long searchClientId,
            Model model) {

        List<ProductDTO> products = new ArrayList<>();

        try {
            if (searchId != null) {
                products.add(productService.findById(searchId));
            } else if (searchName != null && !searchName.isBlank()) {
                productService.findByName(searchName).ifPresent(products::add);
            } else if (searchClientId != null) {
                products = productService.findAllByClientId(searchClientId);
            } else {
                products = productService.findAll();
            }
        } catch (ResourceNotFoundException e) {
            model.addAttribute("error", "No se encontraron productos con ese criterio.");
        }

        model.addAttribute("products", products);

        model.addAttribute("clients", clientService.findAll());

        return "products/list";
    }

    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable Long id, @ModelAttribute ProductDTO productDTO) {
        try {
            // Intentamos actualizar el producto
            productService.update(id, productDTO);
            return "redirect:/products";

        } catch (Exception e) {
            // Si falla, imprimimos el cartel gigante en la consola del IDE
            System.out.println("\n=======================================================");
            System.out.println("💥 ERROR REAL AL ACTUALIZAR EL PRODUCTO 💥");
            System.out.println("=======================================================");
            e.printStackTrace();
            System.out.println("=======================================================\n");

            // Volvemos a lanzar la excepción para que Spring siga su curso
            throw e;
        }
    }

    // --- 3. ELIMINAR ---
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return "redirect:/products";
    }

    // --- 4. CAMBIAR ESTADOS ---
    @PostMapping("/mark-sold/{id}")
    public String markAsSold(@PathVariable Long id) {
        productService.markAsSold(id);
        return "redirect:/products"; // O a la vista del remate, según necesites luego
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


}