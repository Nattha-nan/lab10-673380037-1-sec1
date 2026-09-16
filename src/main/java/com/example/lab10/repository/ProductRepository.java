package com.example.lab10.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.lab10.model.Product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * ProductRepository — In-memory Reactive Repository
 *
 * ✅ โครงสร้างและ annotation ครบแล้ว
 * ❌ TODO: เติม method body ให้ครบทุก method
 *
 * ใช้ ConcurrentHashMap เป็น in-memory storage
 * (ไม่ต่อ Database — เน้นฝึก Mono/Flux)
 *
 * Hint:
 *   - Mono.just(value)          คืนค่าเดียว
 *   - Mono.empty()              คืนเปล่า
 *   - Flux.fromIterable(list)   คืนหลายค่าจาก collection
 */

// ── Repository Layer: จัดการข้อมูลดิบ (CRUD) ──────────────
public class ProductRepository {

    // ── In-memory storage ────────────────────────────────
    private final Map<String, Product> store = new ConcurrentHashMap<>();

    // ── Constructor: ใส่ข้อมูลตัวอย่าง ──────────────────
    public ProductRepository() {
        store.put("1", new Product("1", "iPhone 15 Pro (673380037-1 SEC 1)",
                "Electronics", "Apple", 50, 39900.0, "MEMBER"));
        store.put("2", new Product("2", "MacBook Air M3",
                "Electronics", "Apple", 20, 49900.0, "NONE"));
        store.put("3", new Product("3", "Samsung Galaxy S24",
                "Electronics", "Samsung", 30, 29900.0, "SEASONAL"));
    }

    // ── 1. หา Product 1 รายการ ───────────────────────────
    /**
     * TODO: คืน Mono<Product> จาก store โดยใช้ id
     *       ถ้าไม่พบให้คืน Mono.empty()
     *
     * Hint: store.get(id) คืน Product หรือ null
     *       ถ้า null ให้ใช้ Mono.empty()
     *       ถ้ามีค่าให้ใช้ Mono.just(product)
     */
    public Mono<Product> findById(String id) {
        // TODO: เติม code ตรงนี้
        Product product = store.get(id);
        // ถ้า null ให้คืน Mono.empty() (แทน null โดยตรง — reactive ไม่ใช้ null)
        return product != null ? Mono.just(product) : Mono.empty();
    }

    // ── 2. หา Product ทั้งหมด ────────────────────────────
    /**
     * TODO: คืน Flux<Product> ของทุกรายการใน store
     *
     * Hint: store.values() คืน Collection<Product>
     *       ใช้ Flux.fromIterable(...) แปลงเป็น Flux
     */
    public Flux<Product> findAll() {
        // TODO: เติม code ตรงนี้
        // แปลง Collection ธรรมดาให้กลายเป็น reactive stream
        return Flux.fromIterable(store.values()); // ← แก้บรรทัดนี้
    }

    // ── 3. บันทึก Product ────────────────────────────────
    /**
     * TODO: บันทึก product ลง store แล้วคืน Mono<Product>
     *
     * Hint: store.put(product.getId(), product)
     *       แล้วใช้ Mono.just(product) คืนค่า
     */
    public Mono<Product> save(Product product) {
        // TODO: เติม code ตรงนี้
        store.put(product.getId(), product);
        return Mono.just(product); // ห่อผลลัพธ์กลับเป็น Mono
    }

    // ── 4. ลบ Product ────────────────────────────────────
    /**
     * TODO: ลบ product จาก store แล้วคืน Mono<Void>
     *
     * Hint: store.remove(id)
     *       แล้วใช้ Mono.empty() คืนค่า (Mono<Void>)
     */
    public Mono<Void> deleteById(String id) {
        // TODO: เติม code ตรงนี้
        store.remove(id);
        return Mono.empty(); // ลบเสร็จ ไม่มีค่าคืน จึงเป็น Mono<Void>
    }

    // ── 5. กรองตาม category ──────────────────────────────
    /**
     * TODO: คืน Flux<Product> ที่ category ตรงกัน
     *
     * Hint: findAll()
     *       .filter(p -> p.getCategory().equalsIgnoreCase(category))
     */
    public Flux<Product> findByCategory(String category) {
        // TODO: เติม code ตรงนี้
        return findAll().filter(product -> product.getCategory().equalsIgnoreCase(category)); // กรองแบบ lazy
    }
}
