'use client';
import { motion, useReducedMotion } from 'motion/react';
import BlurText from './BlurText';
export function HeadlineReveal({ text, className = '' }) {
  const reduced = useReducedMotion();
  return reduced ? <span className={className}>{text}</span> : <BlurText text={text} delay={90} direction="bottom" stepDuration={0.25} className={className} />;
}
export function Reveal({ children, className = '', delay = 0 }) {
  return <motion.div className={className} initial={{ opacity: 0, y: 18 }} whileInView={{ opacity: 1, y: 0 }} viewport={{ once: true, amount: 0.1 }} transition={{ duration: 0.45, delay }}>{children}</motion.div>;
}
