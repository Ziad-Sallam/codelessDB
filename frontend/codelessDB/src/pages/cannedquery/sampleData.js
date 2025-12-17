// sampleData.js - Sample data for canned queries

export const sampleQueries = [
  {
    id: "1",
    title: "Get Active Users",
    description: "Retrieves all users who have been active in the last 30 days",
    body: `SELECT 
  u.id,
  u.email,
  u.name,
  COUNT(a.id) as activity_count
FROM users u
LEFT JOIN activities a ON u.id = a.user_id
WHERE a.created_at > NOW() - INTERVAL '30 days'
GROUP BY u.id
ORDER BY activity_count DESC;`,
    updatedAt: new Date("2024-02-01"),
  },
  {
    id: "2",
    title: "Revenue by Month",
    description: "Calculate total revenue grouped by month for the current year",
    body: `SELECT 
  DATE_TRUNC('month', created_at) as month,
  SUM(amount) as total_revenue,
  COUNT(*) as transaction_count
FROM transactions
WHERE EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM CURRENT_DATE)
GROUP BY DATE_TRUNC('month', created_at)
ORDER BY month;`,
    updatedAt: new Date("2024-01-25"),
  },
  {
    id: "3",
    title: "Find Duplicate Records",
    description: "Identify duplicate entries in a table based on email field.",
    body: `SELECT email, COUNT(*) as count
FROM users
GROUP BY email
HAVING COUNT(*) > 1
ORDER BY count DESC;`,
    updatedAt: new Date("2024-02-01"),
  },
  {
    id: "4",
    title: "Top Selling Products",
    description: "Get the top 10 best-selling products by quantity sold",
    body: `SELECT 
  p.id,
  p.name,
  SUM(oi.quantity) as total_sold,
  SUM(oi.quantity * oi.price) as revenue
FROM products p
JOIN order_items oi ON p.id = oi.product_id
GROUP BY p.id, p.name
ORDER BY total_sold DESC
LIMIT 10;`,
    updatedAt: new Date("2024-01-28"),
  },
  {
    id: "5",
    title: "Customer Lifetime Value",
    description: "Calculate total spending per customer",
    body: `SELECT 
  c.id,
  c.name,
  c.email,
  COUNT(o.id) as order_count,
  SUM(o.total) as lifetime_value
FROM customers c
LEFT JOIN orders o ON c.id = o.customer_id
GROUP BY c.id, c.name, c.email
ORDER BY lifetime_value DESC;`,
    updatedAt: new Date("2024-01-30"),
  },
];