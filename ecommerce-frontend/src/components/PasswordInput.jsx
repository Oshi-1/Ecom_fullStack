export default function PasswordInput({
  name,
  value,
  onChange,
  placeholder,
  autoComplete,
  visible,
  onToggle,
  className,
  buttonClassName,
}) {
  return (
    <div className={className}>
      <input
        name={name}
        type={visible ? "text" : "password"}
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        autoComplete={autoComplete}
      />
      <button
        type="button"
        className={buttonClassName}
        onClick={onToggle}
        aria-label={visible ? "Hide password" : "Show password"}
        title={visible ? "Hide password" : "Show password"}
      >
        {visible ? (
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M2.8 3.9 4.1 2.6l17.3 17.3-1.3 1.3-3.2-3.2A10.8 10.8 0 0 1 12 19.2C6.8 19.2 3.2 15.7 1.5 12c.8-1.7 2.1-3.3 3.8-4.5L2.8 3.9Zm7 7 3.3 3.3A2.6 2.6 0 0 0 9.8 10.9Zm2.2-6.1c5.2 0 8.8 3.5 10.5 7.2a12.4 12.4 0 0 1-3.2 4.1l-2.7-2.7A4.9 4.9 0 0 0 10.6 7l-2-2c1.1-.2 2.2-.2 3.4-.2Zm0 2.5c-.3 0-.6 0-.9.1l5.5 5.5c.1-.3.1-.6.1-.9A4.7 4.7 0 0 0 12 7.3Z" />
          </svg>
        ) : (
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M12 4.8c5.2 0 8.8 3.5 10.5 7.2-1.7 3.7-5.3 7.2-10.5 7.2S3.2 15.7 1.5 12C3.2 8.3 6.8 4.8 12 4.8Zm0 2.2c-3.8 0-6.5 2.3-8 5 1.5 2.7 4.2 5 8 5s6.5-2.3 8-5c-1.5-2.7-4.2-5-8-5Zm0 1.5a3.5 3.5 0 1 1 0 7 3.5 3.5 0 0 1 0-7Zm0 2a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3Z" />
          </svg>
        )}
      </button>
    </div>
  );
}
