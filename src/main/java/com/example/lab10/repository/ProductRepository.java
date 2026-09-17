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
public class ProductRepository {

    // ── In-memory storage ────────────────────────────────
    // ConcurrentHashMap ปลอดภัยกับ multi-thread โดยไม่ต้อง synchronized เอง
    // สำคัญในบริบท reactive ที่หลาย request อาจถูกประมวลผลพร้อมกันบน event loop
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
        // store.get(id) เป็นการอ่านค่าแบบ synchronous ธรรมดา (ไม่มี I/O จริง
        // เพราะเป็น in-memory map) จึงไม่ต้อง wrap เป็น async ตั้งแต่ก่อนเรียก
        Product product = store.get(id);

        // กฎเหล็กของ Reactive Streams: ห้ามส่ง null เข้า Mono.just() เด็ดขาด
        // (Mono.just(null) จะ throw NullPointerException ทันที ไม่ใช่ตอน subscribe)
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
        // store.values() คืน Collection<Product> ที่มีอยู่แล้วในหน่วยความจำทั้งหมด
        // Flux.fromIterable(...) เป็นแค่ตัว "ห่อ" (adapter) ให้ collection ธรรมดา
        // กลายเป็น reactive stream เพื่อให้ layer อื่นเชื่อม operator ต่อได้
        return Flux.fromIterable(store.values());
    }

    // ── 3. บันทึก Product ────────────────────────────────
    /**
     * TODO: บันทึก product ลง store แล้วคืน Mono<Product>
     *
     * Hint: store.put(product.getId(), product)
     *       แล้วใช้ Mono.just(product) คืนค่า
     */
    public Mono<Product> save(Product product) {
        // store.put(...) คือ side effect ที่เกิดขึ้นทันทีตอน method ถูกเรียก
        // (ต่างจาก operator ปกติของ Reactor ที่มักจะ lazy รอ subscribe ก่อน)
        store.put(product.getId(), product);

        // ห่อผลลัพธ์สุดท้ายด้วย Mono.just(...) เพื่อคืนกลับไปให้ layer บนใช้ต่อ
        return Mono.just(product);
    }

    // ── 4. ลบ Product ────────────────────────────────────
    /**
     * TODO: ลบ product จาก store แล้วคืน Mono<Void>
     *
     * Hint: store.remove(id)
     *       แล้วใช้ Mono.empty() คืนค่า (Mono<Void>)
     */
    public Mono<Void> deleteById(String id) {
        // ลบออกจาก map จริง ๆ ทันที (ถ้า id ไม่มีอยู่ก็แค่ไม่มีอะไรเกิดขึ้น ไม่ error)
        store.remove(id);

        // Mono<Void> สื่อว่า "งานเสร็จแล้ว แต่ไม่มี payload จะคืน"
        // Mono.empty() ในบริบทนี้ไม่ได้แปลว่า "หาไม่เจอ" เหมือนใน findById()
        return Mono.empty();
    }

    // ── 5. กรองตาม category ──────────────────────────────
    /**
     * TODO: คืน Flux<Product> ที่ category ตรงกัน
     *
     * Hint: findAll()
     *       .filter(p -> p.getCategory().equalsIgnoreCase(category))
     */
    public Flux<Product> findByCategory(String category) {
        // เรียกใช้ findAll() ซ้ำ แทนที่จะเขียน logic ดึงข้อมูลใหม่ทั้งหมด
        // เป็นการ "ต่อยอด" (compose) จาก stream ที่มีอยู่แล้ว
        return findAll().filter(p -> p.getCategory().equalsIgnoreCase(category));
        /* .filter(...) เป็น operator แบบ lazy จริง ๆ จะยังไม่ execute
         จนกว่าจะมีคน subscribe (ตอน Controller คืนค่ากลับไปให้ Spring)*/
                
    }
}