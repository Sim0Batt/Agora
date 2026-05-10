import React from 'react';
import { useNavigate } from 'react-router-dom';
import { DashBoard } from '../components/Dashboard';

export const HomePage = () => {
  const navigate = useNavigate();
  const gotToDashboard = () => {
    navigate('/dashboard/login');
  };
  return (
    <div className="min-h-screen bg-surface text-text">
      <DashBoard />
      <main className="mx-auto max-w-6xl px-4 py-8 text-text-muted">
        HomePage
      </main>
    </div>
  );
};
