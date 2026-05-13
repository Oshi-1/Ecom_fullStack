import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { deleteProduct, getAllProducts, searchProducts } from "../api/ProductApi";
import { addToCart } from "../api/cartApi";
import { useAuth } from "../context/useAuth";
import { handleProductImageError, productImageFallback } from "../utils/productImage";
import styles from "../styles/products.module.css";

export default function ProductsPage() {
  const [allProducts, setAllProducts] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [toast, setToast] = useState(null);
  const [keyword, setKeyword] = useState("");
  const [category, setCategory] = useState("");
  const { user } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const isAdmin = user?.role === "ADMIN";

  useEffect(() => {
    if (location.state?.toast) {
      const timer = window.setTimeout(() => setToast(location.state.toast), 0);
      navigate(location.pathname, { replace: true, state: {} });
      return () => window.clearTimeout(timer);
    }

    return undefined;
  }, [location.pathname, location.state, navigate]);

  useEffect(() => {
    if (!toast) {
      return;
    }

    const timer = window.setTimeout(() => setToast(null), 3200);
    return () => window.clearTimeout(timer);
  }, [toast]);

  useEffect(() => {
    let active = true;

    const loadInitialProducts = async () => {
      try {
        const res = await getAllProducts();
        if (active) {
          setAllProducts(res.data);
          setProducts(res.data);
        }
      } catch {
        if (active) {
          setError("Failed to load products");
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    };

    loadInitialProducts();

    return () => {
      active = false;
    };
  }, []);

  const handleSearch = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError("");

    try {
      const res = await searchProducts(keyword, category);
      setProducts(res.data);
    } catch {
      setError("Search failed");
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setKeyword("");
    setCategory("");
    setProducts(allProducts);
    setToast(null);
  };

  const handleCategoryChange = (nextCategory) => {
    setCategory(nextCategory);
    setToast(null);

    const normalizedKeyword = keyword.trim().toLowerCase();
    const filteredProducts = allProducts.filter((product) => {
      const matchesCategory = !nextCategory || product.category === nextCategory;
      const matchesKeyword = !normalizedKeyword || product.name.toLowerCase().includes(normalizedKeyword);
      return matchesCategory && matchesKeyword;
    });

    setProducts(filteredProducts);
  };

  const handleAddToCart = async (event, product) => {
    event.stopPropagation();
    if (isAdmin) {
      setToast({ type: "error", message: "Admin users manage products and cannot add items to cart" });
      return;
    }

    setToast(null);
    setError("");

    try {
      await addToCart(product.productId, 1);
      setToast({ type: "success", message: `${product.name} added to cart` });
    } catch (err) {
      setToast({ type: "error", message: err.response?.data?.error || "Product could not be added to cart" });
    }
  };

  const handleDeleteProduct = async (event, product) => {
    event.stopPropagation();
    setToast(null);
    setError("");

    try {
      await deleteProduct(product.productId);
      setAllProducts((current) => current.filter((item) => item.productId !== product.productId));
      setProducts((current) => current.filter((item) => item.productId !== product.productId));
      setToast({ type: "success", message: `${product.name} deleted successfully` });
    } catch (err) {
      setToast({ type: "error", message: err.response?.data?.error || "Product could not be deleted" });
    }
  };

  const handleUpdateProduct = (event, productId) => {
    event.stopPropagation();
    navigate(`/products/${productId}/edit`);
  };

  const handleCardKeyDown = (event, productId) => {
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      navigate(`/products/${productId}`);
    }
  };

  const categories = [...new Set(allProducts.map((p) => p.category).filter(Boolean))];

  return (
    <div className={styles.page}>
      {toast && (
        <div className={`${styles.toast} ${toast.type === "error" ? styles.toastError : styles.toastSuccess}`}>
          <strong>{toast.type === "error" ? "Action failed" : "Success"}</strong>
          <span>{toast.message}</span>
        </div>
      )}

      {isAdmin && (
        <div className={styles.adminBanner}>
          <span>Admin UI</span>
          <strong>Product Management</strong>
        </div>
      )}

      <div className={styles.topBar}>
        <h1>{isAdmin ? "Manage Products" : "Explore Products"}</h1>
        <div className={styles.topActions}>
          {isAdmin && (
            <button type="button" onClick={() => navigate("/products/new")}>
              Add Product
            </button>
          )}
          {!isAdmin && (
            <button type="button" onClick={() => navigate("/cart")}>
              Cart
            </button>
          )}
          <button type="button" onClick={() => navigate("/dashboard")}>
            Back
          </button>
        </div>
      </div>

      <form onSubmit={handleSearch} className={styles.searchBar}>
        <input
          type="text"
          placeholder="Search anything..."
          value={keyword}
          onChange={(event) => setKeyword(event.target.value)}
          className={styles.searchInput}
        />

        <select
          value={category}
          onChange={(event) => handleCategoryChange(event.target.value)}
          className={styles.select}
        >
          <option value="">All</option>
          {categories.map((cat) => (
            <option key={cat} value={cat}>
              {cat}
            </option>
          ))}
        </select>

        <button type="submit" className={styles.searchBtn}>
          Search
        </button>
        <button type="button" onClick={handleReset} className={styles.resetBtn}>
          Reset
        </button>
      </form>

      {loading && <p className={styles.message}>Loading...</p>}
      {error && <p className={styles.error}>{error}</p>}
      {!loading && !error && products.length === 0 && (
        <p className={styles.message}>No products found</p>
      )}

      {!loading && !error && (
        <div className={styles.grid}>
          {products.map((product) => (
            <article
              key={product.productId}
              role="button"
              tabIndex="0"
              className={styles.card}
              onClick={() => navigate(`/products/${product.productId}`)}
              onKeyDown={(event) => handleCardKeyDown(event, product.productId)}
            >
              <div className={styles.imageWrapper}>
                <img
                  src={product.imageUrl || productImageFallback(product.name)}
                  alt={product.name}
                  onError={(event) => handleProductImageError(event, product.name)}
                />
                <div className={styles.overlay}>
                  <span>View Details</span>
                </div>
              </div>

              <div className={styles.cardBody}>
                <h3>{product.name}</h3>
                <p>{product.category}</p>

                <div className={styles.cardFooter}>
                  <span>Rs. {product.price}</span>
                  <span className={product.stock > 0 ? styles.stockQuantity : styles.stockEmpty}>
                    {product.stock > 0 ? `${product.stock} Qty` : "Out"}
                  </span>
                </div>

                {isAdmin ? (
                  <div className={styles.adminProductActions}>
                    <button
                      type="button"
                      className={styles.updateProductBtn}
                      onClick={(event) => handleUpdateProduct(event, product.productId)}
                    >
                      Update
                    </button>
                    <button
                      type="button"
                      className={styles.deleteProductBtn}
                      onClick={(event) => handleDeleteProduct(event, product)}
                    >
                      Delete
                    </button>
                  </div>
                ) : (
                  <button
                    type="button"
                    className={styles.addCartBtn}
                    onClick={(event) => handleAddToCart(event, product)}
                    disabled={product.stock <= 0}
                  >
                    Add to Cart
                  </button>
                )}
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  );
}
