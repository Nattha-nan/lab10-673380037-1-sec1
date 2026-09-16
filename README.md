# ⚡ Lab 10: Spring WebFlux & WebClient

**วิชา:** CP353002 หลักการออกแบบและพัฒนาซอฟต์แวร์  
**เรื่อง:** Reactive Programming · Spring WebFlux · Mono · Flux · WebClient  
**อ้างอิง:**
- [Guide to Spring WebFlux — Baeldung](https://www.baeldung.com/spring-webflux)
- [Introduction to Reactive Programming — Project Reactor](https://projectreactor.io/docs/core/release/reference/reactiveProgramming.html)

**นางสาวณัฐนันทน์ บุษดี รหัสนักศึกษา 673380037-1 Section 1**

---

## 📋 วัตถุประสงค์

1. นักศึกษาอธิบายได้ว่า Reactive Programming คืออะไร และแตกต่างจาก Blocking I/O อย่างไร
2. นักศึกษาเข้าใจ **Mono\<T\>** และ **Flux\<T\>** และรู้ว่าควรใช้แบบไหนในสถานการณ์ใด
3. นักศึกษาสร้าง REST API ด้วย **Spring WebFlux** และ `@RestController` ได้
4. นักศึกษาใช้ **WebClient** เรียก HTTP API แบบ non-blocking ได้
5. นักศึกษาใช้ Operators สำคัญ (`map`, `flatMap`, `filter`, `subscribe`) ได้ถูกต้อง

---

## 🧠 รู้หมือไหร่

### 1. Reactive Programming คืออะไร?

> Reactive programming คือการเขียนโปรแกรมแบบ **asynchronous** ที่ตอบสนองต่อ **data stream** โดยไม่บล็อก thread รอผลลัพธ์

| | Blocking (แบบเดิม) | Reactive |
|---|---|---|
| Thread | รอจนกว่าจะได้ผล | ไม่รอ — ไปทำงานอื่น |
| Scalability | 1 thread per request | Few threads, many requests |
| Return type | `String result` | `Mono<String> result` |
| เหมาะกับ | CRUD ทั่วไป | High-concurrency, streaming |

### 2. Publisher → Subscriber Pattern

```
Publisher ──→ [Operator] ──→ [Operator] ──→ Subscriber
(Source)      (transform)    (filter)       (Consumer)
```

**3 Signal ที่ Publisher ส่งได้:**

| Signal | ความหมาย |
|---|---|
| `onNext(T)` | ส่ง data item (เรียกได้ 0 ถึง N ครั้ง) |
| `onError(e)` | เกิด error — stream จบทันที |
| `onComplete()` | stream จบปกติ ไม่มี item อีกแล้ว |

> ⚠️ **กฎสำคัญ:** ไม่มีอะไรเกิดขึ้นจนกว่าจะเรียก `subscribe()` — Publisher เป็นแค่ "blueprint"

---

### 3. Mono\<T\> — 0 หรือ 1 ค่า

```java
// สร้าง Mono
Mono<String> m1 = Mono.just("Hello");          // มีค่า
Mono<String> m2 = Mono.empty();                // ว่าง
Mono<Product> p  = repo.findById("1");         // จาก Repository

// Operators
mono.map(p -> p.getName())                     // แปลงค่า (sync)
    .flatMap(p -> repo.save(p))                // async transform
    .defaultIfEmpty("Not Found")               // fallback ถ้าว่าง
    .onErrorReturn(new Product())              // fallback เมื่อ error
    .subscribe(System.out::println);           // เริ่ม execute
```

**ใช้ Mono เมื่อ:** `findById`, `save`, `update`, `delete` (คืนค่าเดียว)

---

### 4. Flux\<T\> — 0 ถึง N ค่า

```java
// สร้าง Flux
Flux<String>  f1 = Flux.just("A", "B", "C");  // จากค่าตายตัว
Flux<Product> f2 = repo.findAll();             // จาก Repository
Flux<Integer> f3 = Flux.range(1, 10);          // range 1-10

// Operators
flux.map(p -> p.getName())                     // แปลงแต่ละ element
    .filter(name -> name.startsWith("A"))      // กรอง
    .flatMap(id -> repo.findById(id))          // async แปลงแต่ละตัว
    .take(5)                                   // เอาแค่ 5 ตัวแรก
    .collectList()                             // รวมเป็น Mono<List<T>>
    .subscribe(System.out::println);           // เริ่ม execute
```

**ใช้ Flux เมื่อ:** `findAll`, `search`, event stream (คืนหลายค่า)

---

### 5. Rule of Thumb

| คืน | ใช้ |
|---|---|
| 1 ผลลัพธ์ | `Mono<T>` |
| หลายผลลัพธ์ | `Flux<T>` |
| ไม่มีผลลัพธ์ (side-effect) | `Mono<Void>` |

---

### 6. Spring WebFlux — @RestController

```java
@RestController
@RequestMapping("/products")
public class ProductController {

    // GET /products/{id} → Mono<Product>
    @GetMapping("/{id}")
    public Mono<Product> getById(@PathVariable String id) {
        return service.getById(id);
    }

    // GET /products → Flux<Product>
    @GetMapping
    public Flux<Product> getAll() {
        return service.getAll();
    }
}
```

---

### 7. WebClient — เรียก HTTP API แบบ Reactive

```java
WebClient client = WebClient.create("http://localhost:8080");

// GET 1 รายการ → Mono
Mono<Product> p = client.get()
    .uri("/products/{id}", "1")
    .retrieve()
    .bodyToMono(Product.class);

// GET หลายรายการ → Flux
Flux<Product> all = client.get()
    .uri("/products")
    .retrieve()
    .bodyToFlux(Product.class);

// POST ส่งข้อมูล
Mono<Product> saved = client.post()
    .uri("/products")
    .bodyValue(product)
    .retrieve()
    .bodyToMono(Product.class);
```

---

### 8. Operators สรุป

| Operator | ทำอะไร | ตัวอย่าง |
|---|---|---|
| `map` | แปลง T → R (sync) | `.map(p -> p.getName())` |
| `flatMap` | แปลง T → Publisher\<R\> (async) | `.flatMap(id -> repo.findById(id))` |
| `filter` | กรอง element | `.filter(p -> p.getPrice() > 0)` |
| `take` | เอาแค่ N ตัวแรก | `.take(5)` |
| `collectList` | รวมเป็น `Mono<List<T>>` | `.collectList()` |
| `defaultIfEmpty` | fallback ถ้าว่าง | `.defaultIfEmpty("N/A")` |
| `onErrorReturn` | fallback เมื่อ error | `.onErrorReturn(new Product())` |
| `subscribe` | เริ่ม execute stream | `.subscribe(System.out::println)` |

---

## 🛠️ โครงสร้างโปรเจกต์ Template

```
src/main/java/com/example/lab10/
├── Lab10Application.java          ← ✅ มีให้แล้ว
├── AppConfig.java                 ← ✅ มีให้แล้ว (@Bean config)
├── model/
│   └── Product.java               ← ✅ มีให้แล้ว 
├── repository/
│   └── ProductRepository.java     ← ❌ TODO: เติม method body (5 methods)
├── service/
│   └── ProductService.java        ← ❌ TODO: เติม method body (6 methods)
├── controller/
│   └── ProductController.java     ← ✅ getById ทำแล้ว / ❌ TODO: อีก 5 endpoints
└── client/
    └── ProductWebClient.java      ← ✅ getProductById ทำแล้ว / ❌ TODO: อีก 5 methods
```

**Endpoints ที่ต้องทำให้ครบ:**

| Method | URL | Return | สถานะ |
|---|---|---|---|
| GET | `/products/{id}` | `Mono<Product>` | ✅ ทำแล้ว (ตัวอย่าง) |
| GET | `/products` | `Flux<Product>` | ✅ ทำแล้ว |
| POST | `/products` | `Mono<Product>` | ✅ ทำแล้ว |
| DELETE | `/products/{id}` | `Mono<Void>` | ✅ ทำแล้ว |
| GET | `/products/category/{cat}` | `Flux<Product>` | ✅ ทำแล้ว |
| GET | `/products/{id}/price` | `Mono<Double>` | ✅ ทำแล้ว |

---

## 🚀 วิธีรันโปรเจกต์

```bash
# Clone repo
git clone https://github.com/<your-username>/lab10-67XXXXXXXX-X-sec1.git
cd lab10-67XXXXXXXX-X-sec1

# แก้ artifactId ใน pom.xml ให้เป็นรหัสตัวเอง

# รัน
mvn spring-boot:run
```

ทดสอบ: [http://localhost:8080/products](http://localhost:8080/products)

---

## 📝 สิ่งที่ต้องส่ง

- [✅] **GitHub Repository** — ชื่อ `lab10-{รหัสนักศึกษา}-sec{section}` พร้อม commit history
- [✅] **Code ครบทุก TODO** — Repository, Service, Controller, WebClient
- [✅] **ผลลัพธ์ใน response มีชื่อและรหัสนักศึกษา** (ใน Product name ที่ seed ไว้)
- [✅] **Screenshot** ทดสอบทุก endpoint ผ่าน Browser หรือ Postman
- [ ] **ไฟล์ PDF** ชื่อ `Lab10_xxxxSec#.pdf`

**PDF ต้องอธิบาย:**
1. Reactive Programming vs Blocking — ต่างกันอย่างไร
2. Mono vs Flux — ใช้กรณีไหน และทำไม
3. WebClient — อธิบาย method chain `.get()`, `.uri()`, `.retrieve()`, `.bodyToMono/Flux()`
4. Code ของ Repository, Service, Controller พร้อม comment อธิบาย

---

## 🔗 แหล่งอ้างอิง

- [Spring WebFlux Guide — Baeldung](https://www.baeldung.com/spring-webflux)
- [Introduction to Reactive Programming — Project Reactor](https://projectreactor.io/docs/core/release/reference/reactiveProgramming.html)
- [WebClient Reference — Spring Docs](https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html)
