# 📊 ĐÁNH GIÁ CHỨC NĂNG DUYỆT SẢN PHẨM VÀ TÌM KIẾM

**Ngày đánh giá:** 18/11/2025  
**Chức năng:** Duyệt sản phẩm (khóa học) và tìm kiếm  
**Điểm ước tính:** **0.4 - 0.5 / 1.5** (27-33%)

---

## 🎯 TỔNG QUAN

Hệ thống hiện tại chỉ có **các chức năng cơ bản** để xem danh sách và chi tiết khóa học. **Thiếu hoàn toàn** các tính năng quan trọng như: **tìm kiếm**, **lọc**, **sắp xếp**, **phân trang**, **đề xuất** khóa học.

---

## ✅ CÁC CHỨC NĂNG ĐÃ CÓ

### **1. Xem danh sách khóa học đã publish** ✅ **100%**

**Endpoint:**
```
GET /api/courses
```

**Controller:**
```java
@RestController
@RequestMapping("/api/courses")
public class CourseUserController {
    
    @GetMapping
    public ResponseEntity<ResponseMessage<List<Course>>> getAllPublishedCourses() {
        return ResponseEntity.ok(userService.getAllPublishedCourses());
    }
}
```

**Service:**
```java
public ResponseMessage<List<Course>> getAllPublishedCourses() {
    List<Course> list = courseRepository.findByIsPublishedTrue();
    return new ResponseMessage<>(true, "Success", list);
}
```

**Repository:**
```java
public interface CourseRepository extends MongoRepository<Course, String> {
    List<Course> findByIsPublishedTrue();
}
```

**Tính năng:**
- ✅ Lấy tất cả khóa học có `isPublished = true`
- ✅ Không yêu cầu authentication (public API)
- ✅ Trả về đầy đủ thông tin course

**Response mẫu:**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": "691c79f6190d8c0f5aac76a0",
      "categoryCode": "PROGRAM",
      "title": "Khóa học Java Spring Boot từ A-Z",
      "description": "Học Spring Boot từ cơ bản đến nâng cao",
      "price": 1800000.0,
      "thumbnailUrl": "http://localhost:8080/static/courses/course-java.jpg",
      "duration": 50,
      "level": "Advanced",
      "isPublished": true,
      "instructorName": "Nguyễn Văn A",
      "rating": 4.8,
      "totalStudents": 1250,
      "discountPercent": 10,
      "discountedPrice": 1620000.0
    }
  ]
}
```

**Vấn đề:**
- ❌ **Không có phân trang** → Trả về tất cả courses (có thể hàng nghìn records)
- ❌ **Không có limit** → Performance kém khi data lớn
- ⚠️ Frontend phải tự handle pagination

---

### **2. Xem chi tiết 1 khóa học** ✅ **100%**

**Endpoint:**
```
GET /api/courses/{id}
```

**Controller:**
```java
@GetMapping("/{id}")
public ResponseEntity<ResponseMessage<Optional<Course>>> getCourseById(@PathVariable String id) {
    return ResponseEntity.ok(userService.getCourseById(id));
}
```

**Service:**
```java
public ResponseMessage<Optional<Course>> getCourseById(String id) {
    return new ResponseMessage<>(
        true,
        "Success",
        courseRepository.findById(id)
    );
}
```

**Tính năng:**
- ✅ Lấy thông tin chi tiết 1 khóa học theo ID
- ✅ Không yêu cầu authentication
- ✅ Trả về `Optional<Course>` (có thể null nếu không tìm thấy)

**Response mẫu:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": "691c79f6190d8c0f5aac76a0",
    "categoryCode": "PROGRAM",
    "title": "Khóa học Java Spring Boot từ A-Z",
    "description": "Học Spring Boot từ cơ bản đến nâng cao...",
    "price": 1800000.0,
    "thumbnailUrl": "http://localhost:8080/uploads/products/course-java.jpg",
    "duration": 50,
    "level": "Advanced",
    "isPublished": true,
    "instructorName": "Nguyễn Văn A",
    "rating": 4.8,
    "totalStudents": 1250,
    "discountPercent": 10,
    "discountedPrice": 1620000.0
  }
}
```

**Vấn đề:**
- ⚠️ Trả về `Optional<Course>` thay vì `Course` trực tiếp
- ⚠️ Frontend phải check `data != null`

---

### **3. Lọc theo Category** ✅ **50%** (Có repository method nhưng chưa có API)

**Repository:**
```java
public interface CourseRepository extends MongoRepository<Course, String> {
    List<Course> findByIsPublishedTrue();
    List<Course> findByCategoryCode(String categoryCode);  // ← Có sẵn nhưng CHƯA DÙNG
}
```

**Trạng thái:**
- ✅ Repository method đã có
- ❌ **CHƯA có endpoint** để gọi
- ❌ **CHƯA có service** sử dụng method này

**Cần bổ sung:**
```java
// CourseUserController.java
@GetMapping("/category/{categoryCode}")
public ResponseEntity<ResponseMessage<List<Course>>> getCoursesByCategory(
    @PathVariable String categoryCode) {
    return ResponseEntity.ok(userService.getCoursesByCategory(categoryCode));
}

// CourseUserService.java
public ResponseMessage<List<Course>> getCoursesByCategory(String categoryCode) {
    List<Course> list = courseRepository.findByCategoryCode(categoryCode);
    return new ResponseMessage<>(true, "Success", list);
}
```

---

## ❌ CÁC CHỨC NĂNG THIẾU

### **1. Tìm kiếm** ❌ **0%** - **NGHIÊM TRỌNG**

**Yêu cầu thường gặp:**
- Tìm theo tên khóa học
- Tìm theo từ khóa trong description
- Tìm theo instructor
- Tìm kết hợp nhiều điều kiện

**Hiện trạng:**
- ❌ **KHÔNG có endpoint** `/api/courses/search`
- ❌ **KHÔNG có** search repository method
- ❌ **KHÔNG có** full-text search
- ❌ **KHÔNG có** search suggestions

**Cần bổ sung:**

#### **Cách 1: Simple Search (MongoDB Query)**
```java
// CourseRepository.java
public interface CourseRepository extends MongoRepository<Course, String> {
    // Tìm theo title (case-insensitive, partial match)
    List<Course> findByTitleContainingIgnoreCaseAndIsPublishedTrue(String keyword);
    
    // Tìm theo title HOẶC description
    @Query("{ $and: [ " +
           "  { $or: [ " +
           "    { 'title': { $regex: ?0, $options: 'i' } }, " +
           "    { 'description': { $regex: ?0, $options: 'i' } } " +
           "  ]}, " +
           "  { 'isPublished': true } " +
           "]}")
    List<Course> searchCourses(String keyword);
    
    // Tìm theo instructor
    List<Course> findByInstructorNameContainingIgnoreCaseAndIsPublishedTrue(String instructor);
}

// CourseUserService.java
public ResponseMessage<List<Course>> searchCourses(String keyword) {
    if (keyword == null || keyword.trim().isEmpty()) {
        return getAllPublishedCourses();
    }
    
    List<Course> results = courseRepository.searchCourses(keyword.trim());
    return new ResponseMessage<>(true, "Tìm thấy " + results.size() + " khóa học", results);
}

// CourseUserController.java
@GetMapping("/search")
public ResponseEntity<ResponseMessage<List<Course>>> searchCourses(
    @RequestParam(required = false) String keyword) {
    return ResponseEntity.ok(userService.searchCourses(keyword));
}
```

**Test:**
```bash
GET /api/courses/search?keyword=java
GET /api/courses/search?keyword=spring boot
```

---

#### **Cách 2: Full-Text Search (MongoDB Text Index)**

**Bước 1: Tạo Text Index**
```java
// Application.java hoặc Config
@Bean
public CommandLineRunner createTextIndex(MongoTemplate mongoTemplate) {
    return args -> {
        mongoTemplate.indexOps(Course.class)
            .ensureIndex(new TextIndexDefinition.TextIndexDefinitionBuilder()
                .onField("title", 10f)      // Weight cao hơn
                .onField("description", 5f)
                .onField("instructorName", 3f)
                .build());
    };
}
```

**Bước 2: Query**
```java
// CourseRepository.java
@Query("{ $text: { $search: ?0 }, 'isPublished': true }")
List<Course> fullTextSearch(String keyword);

// Service
public ResponseMessage<List<Course>> fullTextSearch(String keyword) {
    List<Course> results = courseRepository.fullTextSearch(keyword);
    return new ResponseMessage<>(true, "Kết quả tìm kiếm", results);
}
```

---

### **2. Lọc (Filter)** ❌ **0%** - **QUAN TRỌNG**

**Yêu cầu:**
- Lọc theo category
- Lọc theo level (Beginner, Intermediate, Advanced)
- Lọc theo giá (min-max)
- Lọc theo rating
- Lọc theo duration
- Kết hợp nhiều filter

**Hiện trạng:**
- ❌ **KHÔNG có** filter parameters
- ❌ **KHÔNG có** dynamic query
- ❌ **KHÔNG có** filter DTO

**Cần bổ sung:**

```java
// CourseFilterRequest.java
@Data
public class CourseFilterRequest {
    private String categoryCode;
    private String level;           // BEGINNER, INTERMEDIATE, ADVANCED
    private Double minPrice;
    private Double maxPrice;
    private Double minRating;
    private Integer minDuration;
    private Integer maxDuration;
}

// CourseUserService.java
public ResponseMessage<List<Course>> filterCourses(CourseFilterRequest filter) {
    Query query = new Query();
    query.addCriteria(Criteria.where("isPublished").is(true));
    
    if (filter.getCategoryCode() != null) {
        query.addCriteria(Criteria.where("categoryCode").is(filter.getCategoryCode()));
    }
    
    if (filter.getLevel() != null) {
        query.addCriteria(Criteria.where("level").is(filter.getLevel()));
    }
    
    if (filter.getMinPrice() != null || filter.getMaxPrice() != null) {
        Criteria priceCriteria = Criteria.where("price");
        if (filter.getMinPrice() != null) {
            priceCriteria.gte(filter.getMinPrice());
        }
        if (filter.getMaxPrice() != null) {
            priceCriteria.lte(filter.getMaxPrice());
        }
        query.addCriteria(priceCriteria);
    }
    
    if (filter.getMinRating() != null) {
        query.addCriteria(Criteria.where("rating").gte(filter.getMinRating()));
    }
    
    List<Course> results = mongoTemplate.find(query, Course.class);
    return new ResponseMessage<>(true, "Lọc thành công", results);
}

// Controller
@GetMapping("/filter")
public ResponseEntity<ResponseMessage<List<Course>>> filterCourses(
    @RequestParam(required = false) String categoryCode,
    @RequestParam(required = false) String level,
    @RequestParam(required = false) Double minPrice,
    @RequestParam(required = false) Double maxPrice,
    @RequestParam(required = false) Double minRating) {
    
    CourseFilterRequest filter = new CourseFilterRequest();
    filter.setCategoryCode(categoryCode);
    filter.setLevel(level);
    filter.setMinPrice(minPrice);
    filter.setMaxPrice(maxPrice);
    filter.setMinRating(minRating);
    
    return ResponseEntity.ok(userService.filterCourses(filter));
}
```

**Test:**
```bash
GET /api/courses/filter?categoryCode=PROGRAM&level=Advanced
GET /api/courses/filter?minPrice=1000000&maxPrice=2000000
GET /api/courses/filter?minRating=4.5
GET /api/courses/filter?categoryCode=PROGRAM&minPrice=1000000&minRating=4.0
```

---

### **3. Sắp xếp (Sort)** ❌ **0%** - **QUAN TRỌNG**

**Yêu cầu:**
- Sắp xếp theo giá (tăng dần/giảm dần)
- Sắp xếp theo rating
- Sắp xếp theo số học viên
- Sắp xếp theo ngày tạo (mới nhất)
- Sắp xếp theo tên A-Z

**Hiện trạng:**
- ❌ **KHÔNG có** sort parameters
- ❌ Luôn trả về theo thứ tự mặc định (insertion order)

**Cần bổ sung:**

```java
// CourseUserService.java
public ResponseMessage<List<Course>> getCoursesSorted(String sortBy, String direction) {
    Sort sort;
    
    // Default: giá tăng dần
    if (sortBy == null || sortBy.isEmpty()) {
        sortBy = "price";
        direction = "asc";
    }
    
    // Tạo Sort object
    if ("desc".equalsIgnoreCase(direction)) {
        sort = Sort.by(Sort.Direction.DESC, sortBy);
    } else {
        sort = Sort.by(Sort.Direction.ASC, sortBy);
    }
    
    Query query = new Query();
    query.addCriteria(Criteria.where("isPublished").is(true));
    query.with(sort);
    
    List<Course> results = mongoTemplate.find(query, Course.class);
    return new ResponseMessage<>(true, "Success", results);
}

// Controller
@GetMapping("/sorted")
public ResponseEntity<ResponseMessage<List<Course>>> getCoursesSorted(
    @RequestParam(defaultValue = "price") String sortBy,
    @RequestParam(defaultValue = "asc") String direction) {
    return ResponseEntity.ok(userService.getCoursesSorted(sortBy, direction));
}
```

**Test:**
```bash
GET /api/courses/sorted?sortBy=price&direction=asc     # Giá thấp → cao
GET /api/courses/sorted?sortBy=price&direction=desc    # Giá cao → thấp
GET /api/courses/sorted?sortBy=rating&direction=desc   # Rating cao nhất
GET /api/courses/sorted?sortBy=totalStudents&direction=desc  # Nhiều học viên nhất
GET /api/courses/sorted?sortBy=title&direction=asc     # A-Z
```

---

### **4. Phân trang (Pagination)** ❌ **0%** - **NGHIÊM TRỌNG**

**Yêu cầu:**
- Giới hạn số records mỗi trang
- Hỗ trợ page number
- Trả về thông tin: total, totalPages, currentPage

**Hiện trạng:**
- ❌ **KHÔNG có phân trang**
- ❌ Luôn trả về **TẤT CẢ** courses
- ❌ Performance kém khi có nhiều data

**Cần bổ sung:**

```java
// CoursePageResponse.java
@Data
@Builder
public class CoursePageResponse {
    private List<Course> courses;
    private long total;
    private int totalPages;
    private int currentPage;
    private int pageSize;
}

// CourseUserService.java
public ResponseMessage<CoursePageResponse> getCoursesWithPagination(
    int page, int size, String sortBy, String direction) {
    
    // Validate
    if (page < 0) page = 0;
    if (size <= 0 || size > 100) size = 20;  // Max 100 per page
    
    // Create Sort
    Sort sort = Sort.by(
        "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC,
        sortBy != null ? sortBy : "createdAt"
    );
    
    // Create Pageable
    Pageable pageable = PageRequest.of(page, size, sort);
    
    // Query with pagination
    Query query = new Query();
    query.addCriteria(Criteria.where("isPublished").is(true));
    query.with(pageable);
    
    List<Course> courses = mongoTemplate.find(query, Course.class);
    
    // Count total
    long total = mongoTemplate.count(
        Query.query(Criteria.where("isPublished").is(true)), 
        Course.class
    );
    
    CoursePageResponse response = CoursePageResponse.builder()
        .courses(courses)
        .total(total)
        .totalPages((int) Math.ceil((double) total / size))
        .currentPage(page)
        .pageSize(size)
        .build();
    
    return new ResponseMessage<>(true, "Success", response);
}

// Controller
@GetMapping("/page")
public ResponseEntity<ResponseMessage<CoursePageResponse>> getCoursesWithPagination(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    @RequestParam(defaultValue = "createdAt") String sortBy,
    @RequestParam(defaultValue = "desc") String direction) {
    
    return ResponseEntity.ok(
        userService.getCoursesWithPagination(page, size, sortBy, direction)
    );
}
```

**Response mẫu:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "courses": [ /* 20 courses */ ],
    "total": 150,
    "totalPages": 8,
    "currentPage": 0,
    "pageSize": 20
  }
}
```

**Test:**
```bash
GET /api/courses/page?page=0&size=20          # Trang 1, 20 items
GET /api/courses/page?page=1&size=10          # Trang 2, 10 items
GET /api/courses/page?page=0&size=20&sortBy=price&direction=asc
```

---

### **5. Tìm kiếm + Lọc + Sắp xếp + Phân trang (Combined)** ❌ **0%** - **QUAN TRỌNG NHẤT**

**Yêu cầu:**
Kết hợp TẤT CẢ các chức năng trên trong 1 endpoint duy nhất.

**Cần bổ sung:**

```java
// CourseSearchRequest.java
@Data
public class CourseSearchRequest {
    // Search
    private String keyword;
    
    // Filter
    private String categoryCode;
    private String level;
    private Double minPrice;
    private Double maxPrice;
    private Double minRating;
    
    // Sort
    private String sortBy;
    private String direction;
    
    // Pagination
    private int page = 0;
    private int size = 20;
}

// CourseUserService.java
public ResponseMessage<CoursePageResponse> searchAndFilter(CourseSearchRequest request) {
    Query query = new Query();
    query.addCriteria(Criteria.where("isPublished").is(true));
    
    // 1. SEARCH
    if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
        String keyword = request.getKeyword().trim();
        Criteria searchCriteria = new Criteria().orOperator(
            Criteria.where("title").regex(keyword, "i"),
            Criteria.where("description").regex(keyword, "i"),
            Criteria.where("instructorName").regex(keyword, "i")
        );
        query.addCriteria(searchCriteria);
    }
    
    // 2. FILTER
    if (request.getCategoryCode() != null) {
        query.addCriteria(Criteria.where("categoryCode").is(request.getCategoryCode()));
    }
    
    if (request.getLevel() != null) {
        query.addCriteria(Criteria.where("level").is(request.getLevel()));
    }
    
    if (request.getMinPrice() != null || request.getMaxPrice() != null) {
        Criteria priceCriteria = Criteria.where("price");
        if (request.getMinPrice() != null) priceCriteria.gte(request.getMinPrice());
        if (request.getMaxPrice() != null) priceCriteria.lte(request.getMaxPrice());
        query.addCriteria(priceCriteria);
    }
    
    if (request.getMinRating() != null) {
        query.addCriteria(Criteria.where("rating").gte(request.getMinRating()));
    }
    
    // 3. SORT
    String sortBy = request.getSortBy() != null ? request.getSortBy() : "createdAt";
    Sort.Direction direction = "desc".equalsIgnoreCase(request.getDirection()) 
        ? Sort.Direction.DESC 
        : Sort.Direction.ASC;
    Sort sort = Sort.by(direction, sortBy);
    
    // 4. PAGINATION
    int page = Math.max(0, request.getPage());
    int size = Math.max(1, Math.min(100, request.getSize()));
    Pageable pageable = PageRequest.of(page, size, sort);
    
    query.with(pageable);
    
    // Execute
    List<Course> courses = mongoTemplate.find(query, Course.class);
    long total = mongoTemplate.count(
        query.limit(-1).skip(-1), // Remove pagination for count
        Course.class
    );
    
    CoursePageResponse response = CoursePageResponse.builder()
        .courses(courses)
        .total(total)
        .totalPages((int) Math.ceil((double) total / size))
        .currentPage(page)
        .pageSize(size)
        .build();
    
    return new ResponseMessage<>(true, "Success", response);
}

// Controller
@PostMapping("/search-advanced")
public ResponseEntity<ResponseMessage<CoursePageResponse>> searchAndFilter(
    @RequestBody CourseSearchRequest request) {
    return ResponseEntity.ok(userService.searchAndFilter(request));
}

// HOẶC dùng GET với query params
@GetMapping("/advanced")
public ResponseEntity<ResponseMessage<CoursePageResponse>> searchAdvanced(
    @RequestParam(required = false) String keyword,
    @RequestParam(required = false) String categoryCode,
    @RequestParam(required = false) String level,
    @RequestParam(required = false) Double minPrice,
    @RequestParam(required = false) Double maxPrice,
    @RequestParam(required = false) Double minRating,
    @RequestParam(defaultValue = "createdAt") String sortBy,
    @RequestParam(defaultValue = "desc") String direction,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size) {
    
    CourseSearchRequest request = new CourseSearchRequest();
    request.setKeyword(keyword);
    request.setCategoryCode(categoryCode);
    request.setLevel(level);
    request.setMinPrice(minPrice);
    request.setMaxPrice(maxPrice);
    request.setMinRating(minRating);
    request.setSortBy(sortBy);
    request.setDirection(direction);
    request.setPage(page);
    request.setSize(size);
    
    return ResponseEntity.ok(userService.searchAndFilter(request));
}
```

**Test:**
```bash
# Tìm "java", category PROGRAM, giá 1-2tr, rating >= 4.5, sắp xếp theo giá tăng, trang 1
GET /api/courses/advanced?keyword=java&categoryCode=PROGRAM&minPrice=1000000&maxPrice=2000000&minRating=4.5&sortBy=price&direction=asc&page=0&size=20

# POST version
POST /api/courses/search-advanced
{
  "keyword": "java spring boot",
  "categoryCode": "PROGRAM",
  "level": "Advanced",
  "minPrice": 1000000,
  "maxPrice": 2000000,
  "minRating": 4.5,
  "sortBy": "rating",
  "direction": "desc",
  "page": 0,
  "size": 10
}
```

---

### **6. Khóa học liên quan (Related Courses)** ❌ **0%**

**Yêu cầu:**
Khi xem 1 khóa học, hiển thị các khóa học liên quan (cùng category, cùng instructor, cùng level...)

**Cần bổ sung:**

```java
// CourseUserService.java
public ResponseMessage<List<Course>> getRelatedCourses(String courseId, int limit) {
    // Lấy course hiện tại
    Course current = courseRepository.findById(courseId)
        .orElseThrow(() -> new RuntimeException("Course not found"));
    
    // Tìm courses liên quan
    Query query = new Query();
    query.addCriteria(Criteria.where("isPublished").is(true));
    query.addCriteria(Criteria.where("_id").ne(courseId));  // Exclude current
    
    // Ưu tiên: cùng category > cùng level > cùng instructor
    Criteria criteria = new Criteria().orOperator(
        Criteria.where("categoryCode").is(current.getCategoryCode()),
        Criteria.where("level").is(current.getLevel()),
        Criteria.where("instructorName").is(current.getInstructorName())
    );
    query.addCriteria(criteria);
    query.limit(limit);
    
    List<Course> related = mongoTemplate.find(query, Course.class);
    return new ResponseMessage<>(true, "Related courses", related);
}

// Controller
@GetMapping("/{id}/related")
public ResponseEntity<ResponseMessage<List<Course>>> getRelatedCourses(
    @PathVariable String id,
    @RequestParam(defaultValue = "6") int limit) {
    return ResponseEntity.ok(userService.getRelatedCourses(id, limit));
}
```

---

### **7. Khóa học phổ biến / Bán chạy** ❌ **0%**

**Yêu cầu:**
- Top khóa học có nhiều học viên nhất
- Top khóa học rating cao nhất
- Khóa học mới nhất

**Cần bổ sung:**

```java
// CourseUserService.java
public ResponseMessage<List<Course>> getPopularCourses(int limit) {
    Query query = new Query();
    query.addCriteria(Criteria.where("isPublished").is(true));
    query.with(Sort.by(Sort.Direction.DESC, "totalStudents"));
    query.limit(limit);
    
    List<Course> courses = mongoTemplate.find(query, Course.class);
    return new ResponseMessage<>(true, "Popular courses", courses);
}

public ResponseMessage<List<Course>> getTopRatedCourses(int limit) {
    Query query = new Query();
    query.addCriteria(Criteria.where("isPublished").is(true));
    query.addCriteria(Criteria.where("rating").gte(4.0));
    query.with(Sort.by(Sort.Direction.DESC, "rating"));
    query.limit(limit);
    
    List<Course> courses = mongoTemplate.find(query, Course.class);
    return new ResponseMessage<>(true, "Top rated courses", courses);
}

public ResponseMessage<List<Course>> getNewestCourses(int limit) {
    Query query = new Query();
    query.addCriteria(Criteria.where("isPublished").is(true));
    query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
    query.limit(limit);
    
    List<Course> courses = mongoTemplate.find(query, Course.class);
    return new ResponseMessage<>(true, "Newest courses", courses);
}

// Controller
@GetMapping("/popular")
public ResponseEntity<ResponseMessage<List<Course>>> getPopularCourses(
    @RequestParam(defaultValue = "10") int limit) {
    return ResponseEntity.ok(userService.getPopularCourses(limit));
}

@GetMapping("/top-rated")
public ResponseEntity<ResponseMessage<List<Course>>> getTopRatedCourses(
    @RequestParam(defaultValue = "10") int limit) {
    return ResponseEntity.ok(userService.getTopRatedCourses(limit));
}

@GetMapping("/newest")
public ResponseEntity<ResponseMessage<List<Course>>> getNewestCourses(
    @RequestParam(defaultValue = "10") int limit) {
    return ResponseEntity.ok(userService.getNewestCourses(limit));
}
```

---

### **8. Autocomplete / Search Suggestions** ❌ **0%**

**Yêu cầu:**
Gợi ý từ khóa khi user đang gõ tìm kiếm.

**Cần bổ sung:**

```java
// CourseUserService.java
public ResponseMessage<List<String>> getSuggestions(String prefix, int limit) {
    if (prefix == null || prefix.length() < 2) {
        return new ResponseMessage<>(true, "No suggestions", List.of());
    }
    
    Query query = new Query();
    query.addCriteria(Criteria.where("isPublished").is(true));
    query.addCriteria(Criteria.where("title").regex("^" + prefix, "i"));
    query.fields().include("title");
    query.limit(limit);
    
    List<Course> courses = mongoTemplate.find(query, Course.class);
    List<String> suggestions = courses.stream()
        .map(Course::getTitle)
        .collect(Collectors.toList());
    
    return new ResponseMessage<>(true, "Suggestions", suggestions);
}

// Controller
@GetMapping("/suggestions")
public ResponseEntity<ResponseMessage<List<String>>> getSuggestions(
    @RequestParam String q,
    @RequestParam(defaultValue = "5") int limit) {
    return ResponseEntity.ok(userService.getSuggestions(q, limit));
}
```

**Test:**
```bash
GET /api/courses/suggestions?q=java
→ ["Java Spring Boot", "Java Core", "Java Advanced"]
```

---

## 📊 BẢNG TỔNG KẾT CHI TIẾT

| # | Chức năng | Trạng thái | % Hoàn thành | Điểm/0.25 | Ghi chú |
|---|-----------|-----------|--------------|-----------|---------|
| 1 | **Xem danh sách courses** | ✅ Có | 50% | **0.13** | Thiếu pagination |
| 2 | **Xem chi tiết 1 course** | ✅ Có | 100% | **0.25** | Hoàn chỉnh |
| 3 | **Tìm kiếm** | ❌ Không | 0% | **0.00** | ❌ THIẾU HOÀN TOÀN |
| 4 | **Lọc (Filter)** | ❌ Không | 10% | **0.02** | Có repo method nhưng chưa API |
| 5 | **Sắp xếp (Sort)** | ❌ Không | 0% | **0.00** | ❌ THIẾU |
| 6 | **Phân trang (Pagination)** | ❌ Không | 0% | **0.00** | ❌ THIẾU NGHIÊM TRỌNG |
| 7 | **Khóa học liên quan** | ❌ Không | 0% | **0.00** | ❌ THIẾU |
| 8 | **Top courses (Popular/Rated)** | ❌ Không | 0% | **0.00** | ❌ THIẾU |

**TỔNG ĐIỂM:** **0.4 / 1.5** ≈ **27%**

---

## 🎯 KHUYẾN NGHỊ ĐỂ ĐẠT ĐIỂM TỐI ĐA

### **PRIORITY 1 - BẮT BUỘC** (để đạt 0.8-0.9 điểm)

#### **1. Phân trang (Pagination)** ⏱️ **30 phút**
Code đầy đủ ở phần [4. Phân trang](#4-phân-trang-pagination--0---nghiêm-trọng)

#### **2. Tìm kiếm cơ bản** ⏱️ **20 phút**
Code đầy đủ ở phần [1. Tìm kiếm - Cách 1](#cách-1-simple-search-mongodb-query)

#### **3. Lọc theo category** ⏱️ **10 phút**
Đã có repository method, chỉ cần thêm endpoint.

---

### **PRIORITY 2 - QUAN TRỌNG** (để đạt 1.1-1.2 điểm)

#### **4. Sắp xếp** ⏱️ **15 phút**
Code ở phần [3. Sắp xếp](#3-sắp-xếp-sort--0---quan-trọng)

#### **5. Lọc nâng cao (giá, rating, level)** ⏱️ **30 phút**
Code ở phần [2. Lọc (Filter)](#2-lọc-filter--0---quan-trọng)

---

### **PRIORITY 3 - NÊN CÓ** (để đạt 1.3-1.5 điểm)

#### **6. Tìm kiếm + Lọc + Sắp xếp + Phân trang** ⏱️ **45 phút**
Code ở phần [5. Combined](#5-tìm-kiếm--lọc--sắp-xếp--phân-trang-combined--0---quan-trọng-nhất)

#### **7. Top courses** ⏱️ **20 phút**
Code ở phần [7. Khóa học phổ biến](#7-khóa-học-phổ-biến--bán-chạy--0)

#### **8. Related courses** ⏱️ **20 phút**
Code ở phần [6. Related Courses](#6-khóa-học-liên-quan-related-courses--0)

---

## 📈 DỰ ĐOÁN ĐIỂM SAU KHI CẢI THIỆN

| Scenario | Chức năng thêm | Điểm ước tính | % |
|----------|----------------|---------------|---|
| **Hiện tại** | - | **0.4 / 1.5** | 27% |
| **+ Pagination** | Phân trang | **0.6 / 1.5** | 40% |
| **+ Basic Search** | Tìm kiếm cơ bản | **0.8 / 1.5** | 53% |
| **+ Filter** | Lọc | **0.95 / 1.5** | 63% |
| **+ Sort** | Sắp xếp | **1.1 / 1.5** | 73% |
| **+ Advanced Search** | Tìm kiếm nâng cao | **1.25 / 1.5** | 83% |
| **+ Top/Related Courses** | Gợi ý khóa học | **1.4 / 1.5** | 93% |
| **+ Full-text Search** | Full-text search | **1.5 / 1.5** | 100% |

---

## 📋 CHECKLIST HÀNH ĐỘNG

### **Để đạt 0.8 điểm (53%):**
- [ ] **Thêm Pagination API** (30 phút) - QUAN TRỌNG NHẤT
- [ ] **Thêm Basic Search** (20 phút)
- [ ] **Thêm Filter by Category** (10 phút)

### **Để đạt 1.1 điểm (73%):**
- [ ] Thêm Sort API (15 phút)
- [ ] Thêm Advanced Filter (30 phút)

### **Để đạt 1.4 điểm (93%):**
- [ ] Tìm kiếm + Lọc + Sắp xếp + Phân trang (45 phút)
- [ ] Top courses API (20 phút)
- [ ] Related courses API (20 phút)

---

## 🎯 KẾT LUẬN

### **Điểm mạnh:**
✅ Có API xem danh sách và chi tiết  
✅ Repository đã có method `findByCategoryCode`  
✅ Cấu trúc code rõ ràng, dễ mở rộng  

### **Điểm yếu nghiêm trọng:**
❌ **KHÔNG có phân trang** → Performance kém khi nhiều data  
❌ **KHÔNG có tìm kiếm** → User không thể tìm khóa học  
❌ **KHÔNG có lọc/sắp xếp** → UX kém  

### **Ưu tiên tuyệt đối:**
🔴 **Thêm Pagination ngay** (30 phút)  
🔴 **Thêm Basic Search** (20 phút)  
🔴 **Thêm Filter & Sort** (45 phút)  

**Với 3 chức năng trên, điểm sẽ tăng từ 0.4 lên ~1.1 (73%)**

---

**Tổng kết:**  
Hệ thống chỉ có **chức năng cơ bản nhất**. Để đạt điểm tốt, **BẮT BUỘC** phải thêm **Pagination, Search, Filter, Sort**. Đây là các tính năng **THIẾT YẾU** của bất kỳ hệ thống e-commerce/e-learning nào.

---

**Ngày đánh giá:** 18/11/2025  
**Người đánh giá:** Backend Analysis Team  
**Phiên bản:** 1.0

