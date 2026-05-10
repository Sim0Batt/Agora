import { HomePage } from './pages/HomePage';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import LatexEditor from './pages/LatexEditor';

const router = createBrowserRouter([
  {
    path: '/',
    element: <HomePage />,
  },
  {
    path: '/latex',
    element: <LatexEditor />,
  },
]);

function App() {
  return <RouterProvider router={router} />;
}

export default App;
