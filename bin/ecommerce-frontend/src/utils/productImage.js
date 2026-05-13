export function productImageFallback(name = "Product") {
  const safeName = String(name || "Product").slice(0, 28);
  const initials = safeName
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((word) => word[0]?.toUpperCase())
    .join("") || "P";

  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="900" height="650" viewBox="0 0 900 650">
      <defs>
        <linearGradient id="bg" x1="0" x2="1" y1="0" y2="1">
          <stop offset="0%" stop-color="#0f766e"/>
          <stop offset="55%" stop-color="#0ea5e9"/>
          <stop offset="100%" stop-color="#1e293b"/>
        </linearGradient>
      </defs>
      <rect width="900" height="650" fill="url(#bg)"/>
      <rect x="90" y="85" width="720" height="480" rx="34" fill="rgba(255,255,255,0.14)" stroke="rgba(255,255,255,0.28)" stroke-width="3"/>
      <circle cx="450" cy="285" r="96" fill="rgba(255,255,255,0.22)"/>
      <text x="450" y="315" text-anchor="middle" font-family="Arial, sans-serif" font-size="78" font-weight="800" fill="#ffffff">${initials}</text>
      <text x="450" y="450" text-anchor="middle" font-family="Arial, sans-serif" font-size="38" font-weight="700" fill="#ffffff">${safeName}</text>
    </svg>
  `;

  return `data:image/svg+xml;utf8,${encodeURIComponent(svg)}`;
}

export function handleProductImageError(event, name) {
  event.currentTarget.onerror = null;
  event.currentTarget.src = productImageFallback(name);
}
