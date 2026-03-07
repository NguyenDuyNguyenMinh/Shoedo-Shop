package poly.edu.controller.cart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.dto.cart.AddToCartRequestDTO;
import poly.edu.dto.cart.CartItemDTO;
import poly.edu.dto.cart.UpdateCartQuantityDTO;
import poly.edu.service.cart.CartService;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * GET /api/cart/{maKH}
     * Lấy danh sách sản phẩm trong giỏ hàng của khách
     */
    @GetMapping("/{maKH}")
    public ResponseEntity<List<CartItemDTO>> getCartItems(@PathVariable Integer maKH) {
        List<CartItemDTO> cartItems = cartService.getCartItems(maKH);
        return ResponseEntity.ok(cartItems);
    }

    /**
     * POST /api/cart/add
     * Thêm sản phẩm vào giỏ hàng
     * Body: { "maKH": 1, "maSKU": 5, "soLuong": 2 }
     */
    @PostMapping("/add")
    public ResponseEntity<CartItemDTO> addToCart(@RequestBody AddToCartRequestDTO request) {
        CartItemDTO item = cartService.addToCart(
                request.getMaKH(),
                request.getMaSKU(),
                request.getSoLuong()
        );
        return ResponseEntity.ok(item);
    }

    /**
     * PUT /api/cart/update
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     * Body: { "maGH": 1, "soLuong": 5 }
     */
    @PutMapping("/update")
    public ResponseEntity<CartItemDTO> updateQuantity(@RequestBody UpdateCartQuantityDTO request) {
        CartItemDTO item = cartService.updateQuantity(request.getMaGH(), request.getSoLuong());
        if (item == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(item);
    }

    /**
     * DELETE /api/cart/{maGH}
     * Xóa sản phẩm khỏi giỏ hàng
     */
    @DeleteMapping("/{maGH}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Integer maGH) {
        cartService.removeFromCart(maGH);
        return ResponseEntity.noContent().build();
    }
}
