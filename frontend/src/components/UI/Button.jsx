import React from 'react';

export default function Button({ children, variant = 'primary', size = '', className = '', icon, full, ...props }) {
  const classes = [
    'btn',
    variant === 'primary' ? 'btn-primary' : variant === 'ghost' ? 'btn-ghost' : variant === 'danger' ? 'btn-danger' : variant === 'icon' ? 'btn-icon' : 'btn-primary',
    size === 'sm' ? 'btn-sm' : size === 'lg' ? 'btn-lg' : '',
    full ? 'btn-full' : '',
    className,
  ].filter(Boolean).join(' ');

  if (variant === 'icon') {
    return <button className={`btn-icon ${className}`} {...props}>{children}</button>;
  }

  return (
    <button className={classes} {...props}>
      {icon && icon}
      {children}
    </button>
  );
}
