import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { deleteProduct, getAllProducts, searchProducts } from "../api/ProductApi";
import { addToCart } from "../api/cartApi";
import { useAuth } from "../context/useAuth";
import { useNotification } from "../context/useNotification";
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
  const [brand, setBrand] = useState("");
  const [minPrice, setMinPrice] = useState("");
  const [maxPrice, setMaxPrice] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const { user } = useAuth();
  const { showSuccess, showError } = useNotification();
  const location = useLocation();
  const navigate = useNavigate();
  const isAdmin = user?.role === "ADMIN";

  useEffect(() => {
    if (location.state?.toast) {
      const { type, message } = location.state.toast;
      const timer = window.setTimeout(() => {
        setToast(location.state.toast);
        if (type === "error") {
          showError(message);
        } else {
          showSuccess(message);
        }
      }, 0);
      navigate(location.pathname, { replace: true, state: {} });
      return () => window.clearTimeout(timer);
    }

    return undefined;
  }, [location.pathname, location.state, navigate, showError, showSuccess]);

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
          showError("Failed to load products.");
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

  const productBrand = (product) => product.brand || product.name.split(" ")[0] || "Generic";
  const pageSize = 6;

  const applyClientFilters = (sourceProducts, nextFilters = {}) => {
    const selectedBrand = nextFilters.brand ?? brand;
    const selectedMinPrice = nextFilters.minPrice ?? minPrice;
    const selectedMaxPrice = nextFilters.maxPrice ?? maxPrice;

    return sourceProducts.filter((product) => {
      const productPrice = Number(product.price);
      const matchesBrand = !selectedBrand || productBrand(product) === selectedBrand;
      const matchesMinPrice = !selectedMinPrice || productPrice >= Number(selectedMinPrice);
      const matchesMaxPrice = !selectedMaxPrice || productPrice <= Number(selectedMaxPrice);
      return matchesBrand && matchesMinPrice && matchesMaxPrice;
    });
  };

  const handleSearch = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    setCurrentPage(1);

    try {
      const res = await searchProducts(keyword, category);
      setProducts(applyClientFilters(res.data));
    } catch {
      setError("Search failed");
      showError("Search failed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setKeyword("");
    setCategory("");
    setBrand("");
    setMinPrice("");
    setMaxPrice("");
    setCurrentPage(1);
    setProducts(allProducts);
    setToast(null);
  };

  const updateFilters = (nextFilters) => {
    const nextKeyword = nextFilters.keyword ?? keyword;
    const nextCategory = nextFilters.category ?? category;
    const normalizedKeyword = nextKeyword.trim().toLowerCase();
    const baseProducts = allProducts.filter((product) => {
      const matchesCategory = !nextCategory || product.category === nextCategory;
      const matchesKeyword = !normalizedKeyword || product.name.toLowerCase().includes(normalizedKeyword);
      return matchesCategory && matchesKeyword;
    });

    setCurrentPage(1);
    setProducts(applyClientFilters(baseProducts, nextFilters));
    setToast(null);
  };

  const handleCategoryChange = (nextCategory) => {
    setCategory(nextCategory);
    updateFilters({ category: nextCategory });
  };

  const handleBrandChange = (nextBrand) => {
    setBrand(nextBrand);
    updateFilters({ brand: nextBrand });
  };

  const handleMinPriceChange = (nextMinPrice) => {
    setMinPrice(nextMinPrice);
    updateFilters({ minPrice: nextMinPrice });
  };

  const handleMaxPriceChange = (nextMaxPrice) => {
    setMaxPrice(nextMaxPrice);
    updateFilters({ maxPrice: nextMaxPrice });
  };

  const handleAddToCart = async (event, product) => {
    event.stopPropagation();
    if (isAdmin) {
      setToast({ type: "error", message: "Admin users manage products and cannot add items to cart" });
      showError("Admin users manage products and cannot add items to cart.");
      return;
    }

    setToast(null);
    setError("");

    try {
      await addToCart(product.productId, 1);
      setToast({ type: "success", message: `${product.name} added to cart` });
      showSuccess(`${product.name} added to cart.`);
    } catch (err) {
      const message = err.response?.data?.error || "Product could not be added to cart";
      setToast({ type: "error", message });
      showError(message);
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
      showSuccess(`${product.name} deleted successfully.`);
    } catch (err) {
      const message = err.response?.data?.error || "Product could not be deleted";
      setToast({ type: "error", message });
      showError(message);
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
  const brands = [...new Set(allProducts.map((p) => productBrand(p)).filter(Boolean))];
  const totalPages = Math.max(1, Math.ceil(products.length / pageSize));
  const pageStart = (currentPage - 1) * pageSize;
  const paginatedProducts = products.slice(pageStart, pageStart + pageSize);

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
        <div>
          <span className={styles.heroEyebrow}>{isAdmin ? "Private Atelier" : "Cartora Studio"}</span>
          <h1>{isAdmin ? "Manage the collection with calm precision." : "Curated essentials for modern living."}</h1>
          <p>
            {isAdmin
              ? "Edit product stories, pricing, and availability from a refined command surface."
              : "Discover elevated pieces with quiet details, tactile finishes, and effortless everyday polish."}
          </p>
        </div>
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

      {!isAdmin && (
        <section className={styles.editorialStrip}>
          <div>
            <span>01</span>
            <strong>Featured Collections</strong>
            <p>Seasonless silhouettes and refined everyday pieces.</p>
          </div>
          <div>
            <span>02</span>
            <strong>Premium Finish</strong>
            <p>Clean materials, softer tones, and enduring detail.</p>
          </div>
          <div>
            <span>03</span>
            <strong>Fast Checkout</strong>
            <p>A polished buying flow from cart to confirmation.</p>
          </div>
        </section>
      )}

      <form onSubmit={handleSearch} className={styles.searchBar}>
        <input
          type="text"
          placeholder="Search anything..."
          value={keyword}
          onChange={(event) => {
            setKeyword(event.target.value);
            updateFilters({ keyword: event.target.value });
          }}
          className={styles.searchInput}
        />

        <select
          aria-label="Category"
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

        <select
          aria-label="Brand"
          value={brand}
          onChange={(event) => handleBrandChange(event.target.value)}
          className={styles.select}
        >
          <option value="">All Brands</option>
          {brands.map((item) => (
            <option key={item} value={item}>
              {item}
            </option>
          ))}
        </select>

        <input
          aria-label="Minimum price"
          type="number"
          min="0"
          step="1"
          placeholder="Min price"
          value={minPrice}
          onChange={(event) => handleMinPriceChange(event.target.value)}
          className={styles.searchInput}
        />

        <input
          aria-label="Maximum price"
          type="number"
          min="0"
          step="1"
          placeholder="Max price"
          value={maxPrice}
          onChange={(event) => handleMaxPriceChange(event.target.value)}
          className={styles.searchInput}
        />

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
          {paginatedProducts.map((product) => (
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
                  loading="lazy"
                  src={product.imageUrl || productImageFallback(product.name)}
                  alt={product.name}
                  onError={(event) => handleProductImageError(event, product.name)}
                />
                {!isAdmin && <button type="button" className={styles.wishlistBtn} onClick={(event) => event.stopPropagation()}>♡</button>}
                <div className={styles.overlay}>
                  <span>View Details</span>
                </div>
              </div>

              <div className={styles.cardBody}>
                <h3>{product.name}</h3>
                <p>{product.category}</p>
                <p data-testid="product-brand">{productBrand(product)}</p>

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

      {!loading && !error && products.length > pageSize && (
        <nav className={styles.pagination} aria-label="Product pagination">
          <button type="button" onClick={() => setCurrentPage((page) => Math.max(1, page - 1))} disabled={currentPage === 1}>
            Previous
          </button>
          <span>
            Page {currentPage} of {totalPages}
          </span>
          <button type="button" onClick={() => setCurrentPage((page) => Math.min(totalPages, page + 1))} disabled={currentPage === totalPages}>
            Next
          </button>
        </nav>
      )}

      {!isAdmin && !loading && !error && products.length > 0 && (
        <>
          <section className={styles.storySection}>
            <div>
              <span className={styles.heroEyebrow}>Brand Journal</span>
              <h2>Designed for a quieter kind of luxury.</h2>
              <p>
                Cartora pairs modern utility with editorial styling, giving every product enough room to breathe and
                every customer a smoother path to purchase.
              </p>
            </div>
            <div className={styles.storyImage} aria-hidden="true" />
          </section>

          <section className={styles.newsletter}>
            <span>Private List</span>
            <h2>Get early access to new drops.</h2>
            <form>
              <input type="email" placeholder="Email address" />
              <button type="button">Join</button>
            </form>
          </section>
        </>
      )}
    </div>
  );
}
