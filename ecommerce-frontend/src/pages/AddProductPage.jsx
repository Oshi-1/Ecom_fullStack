import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { addProduct, getProductById, updateProduct } from "../api/ProductApi";
import { useNotification } from "../context/useNotification";
import styles from "../styles/products.module.css";

const initialForm = {
  name: "",
  description: "",
  price: "",
  stock: "",
  category: "",
  brand: "",
  imageUrl: "",
};

function getApiError(err, fallback) {
  const data = err.response?.data;

  if (!data) {
    return fallback;
  }

  if (typeof data === "string") {
    return data;
  }

  if (data.error) {
    return data.error;
  }

  const messages = Object.values(data).filter(Boolean);
  return messages.length > 0 ? messages.join(", ") : fallback;
}

export default function AddProductPage() {
  const navigate = useNavigate();
  const { showError } = useNotification();
  const { id } = useParams();
  const isEditMode = Boolean(id);
  const [form, setForm] = useState(initialForm);
  const [loading, setLoading] = useState(isEditMode);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!isEditMode) {
      return undefined;
    }

    let active = true;

    const loadProduct = async () => {
      try {
        const res = await getProductById(id);
        if (active) {
          setForm({
            name: res.data.name || "",
            description: res.data.description || "",
            price: res.data.price ?? "",
            stock: res.data.stock ?? "",
            category: res.data.category || "",
            brand: res.data.brand || "",
            imageUrl: res.data.imageUrl || "",
          });
        }
      } catch (err) {
        if (active) {
          const message = getApiError(err, "Product could not be loaded");
          setError(message);
          showError(message);
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    };

    loadProduct();

    return () => {
      active = false;
    };
  }, [id, isEditMode]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError("");

    try {
      const payload = {
        ...form,
        price: Number(form.price),
        stock: Number(form.stock),
      };
      const res = isEditMode ? await updateProduct(id, payload) : await addProduct(payload);
      navigate(`/products/${res.data.productId}`, {
        state: {
          toast: {
            type: "success",
            message: `${payload.name} ${isEditMode ? "updated" : "added"} successfully`,
          },
        },
      });
    } catch (err) {
      const message = getApiError(err, `Product could not be ${isEditMode ? "updated" : "added"}`);
      setError(message);
      showError(message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className={styles.page}>
      <div className={styles.topBar}>
        <h1>{isEditMode ? "Update Product" : "Add Product"}</h1>
        <button type="button" onClick={() => navigate("/products")}>
          Back
        </button>
      </div>

      {loading && <p className={styles.message}>Loading product...</p>}

      {!loading && (
      <form className={styles.productForm} onSubmit={handleSubmit}>
        <input name="name" placeholder="Product name" value={form.name} onChange={handleChange} required />
        <input name="category" placeholder="Category" value={form.category} onChange={handleChange} required />
        <input name="brand" placeholder="Brand" value={form.brand} onChange={handleChange} required />
        <input name="price" type="number" min="1" step="0.01" placeholder="Price" value={form.price} onChange={handleChange} required />
        <input name="stock" type="number" min="0" placeholder="Stock" value={form.stock} onChange={handleChange} required />
        <input name="imageUrl" placeholder="Image URL" value={form.imageUrl} onChange={handleChange} />
        <textarea name="description" placeholder="Description" value={form.description} onChange={handleChange} rows="5" />

        {error && <p className={styles.errorCompact}>{error}</p>}

        <button className={styles.searchBtn} type="submit" disabled={saving}>
          {saving ? "Saving..." : isEditMode ? "Update" : "Add"}
        </button>
      </form>
      )}
    </div>
  );
}
