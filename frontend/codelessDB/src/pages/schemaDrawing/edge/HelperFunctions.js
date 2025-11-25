import { Position } from "@xyflow/react";

export function getIntersection(n, n2) {
  const w = n.measured.width || 0;
  const h = n.measured.height || 0;
  const x = n.internals.positionAbsolute.x;
  const y = n.internals.positionAbsolute.y;

  const w2 = n2.measured.width || 0;
  const h2 = n2.measured.height || 0;
  const x2 = n2.internals.positionAbsolute.x;
  const y2 = n2.internals.positionAbsolute.y;

  const xx1 = x + w / 2;
  const yy1 = y + h / 2;
  const xx2 = x2 + w2 / 2;
  const yy2 = y2 + h2 / 2;

  const dx = xx2 - xx1;
  const dy = yy2 - yy1;

  if (dx === 0 && dy === 0) return { x: xx1, y: yy1 };

  const slope = dy / (dx || 1);

  if (Math.abs(dx) * h > Math.abs(dy) * w) {
    const hitX = dx > 0 ? x + w : x;
    const hitY = yy1 + slope * (hitX - xx1);
    return { x: hitX, y: hitY };
  } else {
    const hitY = dy > 0 ? y + h : y;
    const hitX = xx1 + (hitY - yy1) / slope;
    return { x: hitX, y: hitY };
  }
}

export function getEdgePosition(node, intersectionPoint) {
  const n = node.internals.positionAbsolute;
  const w = node.measured.width || 0;
  const h = node.measured.height || 0;
  const x = intersectionPoint.x;
  const y = intersectionPoint.y;
  const tolerance = 2;

  if (Math.abs(y - n.y) < tolerance) return Position.Top;
  if (Math.abs(y - (n.y + h)) < tolerance) return Position.Bottom;
  if (Math.abs(x - n.x) < tolerance) return Position.Left;
  if (Math.abs(x - (n.x + w)) < tolerance) return Position.Right;

  return Position.Top;
}

export function getEdgeParams(source, target) {
  const sourceIntersection = getIntersection(source, target);
  const targetIntersection = getIntersection(target, source);

  const sourcePos = getEdgePosition(source, sourceIntersection);
  const targetPos = getEdgePosition(target, targetIntersection);

  return {
    sx: sourceIntersection.x,
    sy: sourceIntersection.y,
    tx: targetIntersection.x,
    ty: targetIntersection.y,
    sourcePos,
    targetPos,
  };
}

export default function getLabelCoords(x, y, pos) {
  const offset = 12; // Distance from the border
  if (pos === Position.Top) return { x, y: y - offset };
  if (pos === Position.Bottom) return { x, y: y + offset };
  if (pos === Position.Left) return { x: x - offset, y };
  if (pos === Position.Right) return { x: x + offset, y };
  return { x, y };
}