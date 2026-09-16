package com.example.lab10.service;

import org.springframework.stereotype.Service;

import com.example.lab10.model.Product;
import com.example.lab10.repository.ProductRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * ProductService — Business Logic Layer
 *
 * ✅ @Service, Constructor Injection ครบแล้ว (DIP — SOLID)
 * ❌ TODO: เติม method body ให้ครบทุก method
 *
 * หน้าที่: รับ request จาก Controller → เรียก Repository → คืนผล
 * (SRP — แต่ละ class มีหน้าที่เดียว)
 *
 * Hint Operators ที่ควรใช้:
 *   .map(p -> ...)            แปลงค่า
 *   .flatMap(p -> ...)        async transform
 *   .defaultIfEmpty(...)      fallback ถ้าว่าง
 *   .switchIfEmpty(Mono...)   fallback Mono ถ้าว่าง
 */

// ── Service Layer: business logic, ไม่ยุ่งกับ storage โดยตรง ──
@Service
public class ProductService {

    // ── Constructor Injection (DIP — SOLID) ─────────────
    private final ProductRepository repository; // Dependency Injection (DIP)

    public ProductService(ProductRepository repository) {
        // ถ้า repository คืน empty → แปลงเป็น error แทน (business rule)
        this.repository = repository; 
    }

    // ── 1. ดึง Product 1 รายการ ──────────────────────────
    /**
     * TODO: เรียก repository.findById(id) แล้วคืนผล
     *       ถ้าไม่พบให้ throw RuntimeException("Product not found: " + id)
     *
     * Hint: repository.findById(id)
     *       .switchIfEmpty(Mono.error(new RuntimeException(...)))
     */
    public Mono<Product> getById(String id) {
        // TODO: เติม code ตรงนี้
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Product not found: " + id)));
    }

    // ── 2. ดึง Product ทั้งหมด ───────────────────────────
    /**
     * TODO: เรียก repository.findAll() แล้วคืนผล
     */
    public Flux<Product> getAll() {
        // TODO: เติม code ตรงนี้
        return repository.findAll();
    }

    // ── 3. บันทึก Product ────────────────────────────────
    /**
     * TODO: เรียก repository.save(product) แล้วคืนผล
     *
     * เพิ่มเติม: ถ้า product.getId() เป็น null ให้ generate id ใหม่
     * Hint: java.util.UUID.randomUUID().toString()
     */
    public Mono<Product> save(Product product) {
        // TODO: เติม code ตรงนี้
        if (product.getId() == null) {
            product.setId(java.util.UUID.randomUUID().toString()); // generate id ให้อัตโนมัติ
        }
        return repository.save(product);
    }

    // ── 4. ลบ Product ────────────────────────────────────
    /**
     * TODO: เรียก repository.deleteById(id) แล้วคืนผล
     */
    public Mono<Void> delete(String id) {
        // TODO: เติม code ตรงนี้
        return repository.deleteById(id);
    }

    // ── 5. กรองตาม category ──────────────────────────────
    /**
     * TODO: เรียก repository.findByCategory(category) แล้วคืนผล
     */
    public Flux<Product> getByCategory(String category) {
        // TODO: เติม code ตรงนี้
        return repository.findByCategory(category);
    }

    // ── 6. คำนวณราคาหลังส่วนลด ───────────────────────────
    /**
     * TODO: หา Product จาก id แล้วคืน discountedPrice
     *
     * Hint: getById(id)
     *       .map(p -> p.getDiscountedPrice())
     */
    public Mono<Double> getDiscountedPrice(String id) {
        // TODO: เติม code ตรงนี้
        return getById(id)
                .map(Product::getDiscountedPrice); // แปลง Product → Double โดยไม่ block
    }
}
