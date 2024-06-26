import TasksList from './TasksList';
import {
  QueryClient,
  QueryClientProvider,
} from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import './App.css';

const queryClient = new QueryClient()

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <TasksList />
      <ReactQueryDevtools initialIsOpen={false} />
    </QueryClientProvider>
  )
}

