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
          <stop offset="0%" stop-color="#fffaf2"/>
          <stop offset="62%" stop-color="#ffe9dc"/>
          <stop offset="100%" stop-color="#e4f3ec"/>
        </linearGradient>
        <filter id="shadow" x="-20%" y="-20%" width="140%" height="140%">
          <feDropShadow dx="0" dy="24" stdDeviation="22" flood-color="#44301f" flood-opacity="0.18"/>
        </filter>
      </defs>
      <rect width="900" height="650" fill="url(#bg)"/>
      <rect x="96" y="74" width="708" height="502" rx="28" fill="#ffffff" stroke="#eadfce" stroke-width="4" filter="url(#shadow)"/>
      <rect x="170" y="140" width="560" height="270" rx="26" fill="#1f2933"/>
      <circle cx="304" cy="244" r="58" fill="#ef6a52"/>
      <rect x="392" y="194" width="238" height="38" rx="19" fill="#fffaf2"/>
      <rect x="392" y="258" width="304" height="34" rx="17" fill="#e4f3ec"/>
      <rect x="392" y="318" width="178" height="32" rx="16" fill="#ffd8cf"/>
      <text x="304" y="270" text-anchor="middle" font-family="Arial, sans-serif" font-size="68" font-weight="900" fill="#ffffff">${initials}</text>
      <text x="450" y="485" text-anchor="middle" font-family="Arial, sans-serif" font-size="38" font-weight="900" fill="#1f2933">${safeName}</text>
      <text x="450" y="526" text-anchor="middle" font-family="Arial, sans-serif" font-size="22" font-weight="700" fill="#176b5b">Cartora product</text>
    </svg>
  `;

  return `data:image/svg+xml;utf8,${encodeURIComponent(svg)}`;
}

export function handleProductImageError(event, name) {
  event.currentTarget.onerror = null;
  event.currentTarget.src = productImageFallback(name);
}
