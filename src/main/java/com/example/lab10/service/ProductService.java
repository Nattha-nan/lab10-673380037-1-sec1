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
@Service
public class ProductService {

    // ── Constructor Injection (DIP — SOLID) ─────────────
    // Service ไม่ new ProductRepository() เองข้างใน แต่รับผ่าน constructor
    // ทำให้ทดสอบด้วย mock repository ได้ง่าย และ swap implementation ได้ในอนาคต
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
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
        return repository.findById(id).switchIfEmpty(Mono.error(new RuntimeException("Product not found: " + id)));
                /* switchIfEmpty รับ Mono มาแทนที่ ถ้า upstream (findById) จบแบบ empty
                 Mono.error(...) หมายถึง "จบด้วยข้อผิดพลาด" แทนที่จะจบเฉย ๆ
                 นี่คือจุดที่ business rule ถูกเพิ่ม: "ถ้าไม่เจอ ต้องถือว่าเป็น error"
                 ซึ่งเป็นการตัดสินใจของ Service ไม่ใช่หน้าที่ของ Repository */      
    }

    // ── 2. ดึง Product ทั้งหมด ───────────────────────────
    /**
     * TODO: เรียก repository.findAll() แล้วคืนผล
     */
    public Flux<Product> getAll() {
        // ไม่มี business rule เพิ่มเติม แค่ pass-through ไปยัง repository ตรง ๆ
        // (รักษาโครงสร้าง layer ให้ Controller ไม่ข้าม Service ไปเรียก repository ตรง ๆ)
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
        // Business rule: ถ้า client ไม่ได้ส่ง id มา (กรณี POST สร้างใหม่)
        // ให้ระบบ generate id ให้เอง แทนที่จะปล่อยให้เป็น null แล้วพังตอน
        // เอาไปใช้เป็น key ของ Map ใน Repository
        if (product.getId() == null) {
            product.setId(java.util.UUID.randomUUID().toString());
        }
        return repository.save(product);
    }

    // ── 4. ลบ Product ────────────────────────────────────
    /**
     * TODO: เรียก repository.deleteById(id) แล้วคืนผล
     */
    public Mono<Void> delete(String id) {
        // ไม่มี validation เพิ่ม (ไม่เช็คว่ามี id นี้จริงไหมก่อนลบ)
        // ต่างจาก getById ที่ต้องแปลง empty เป็น error
        return repository.deleteById(id);
    }

    // ── 5. กรองตาม category ──────────────────────────────
    /**
     * TODO: เรียก repository.findByCategory(category) แล้วคืนผล
     */
    public Flux<Product> getByCategory(String category) {
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
        // เรียก getById(id) ของตัวเอง (ไม่เรียก repository ตรง ๆ) เพื่อได้
        // ผลพลอยได้ฟรี ๆ คือ switchIfEmpty(...) ที่แปลง "หาไม่เจอ" เป็น error
        // ทำให้ไม่ต้องเขียนเช็คซ้ำอีกรอบใน method นี้
        return getById(id).map(Product::getDiscountedPrice);
        // .map(Product::getDiscountedPrice) แปลง Mono<Product> → Mono<Double>
        // แบบ synchronous (ไม่มี I/O เพิ่ม เพราะเป็นแค่การคำนวณในหน่วยความจำ)
    }
}