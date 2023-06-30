import { useState } from 'react';
import {
    useQuery,
    useMutation,
    useQueryClient,
} from '@tanstack/react-query';
import ReactModal from 'react-modal';
import TaskEditor from "./TaskEditor"

export default function TasksList() {
    const baseUrl = env.BACKEND_URL
    const tasksUrl = baseUrl + "/" + "task"

    const [updatingTask, setUpdatingTask] = useState(null);
    const [isOpenCreateTaskWindow, setOpenCreateTaskWindow] = useState(false);

    const queryClient = useQueryClient();

    const { isLoading, isError, data, error } = useQuery({
        queryKey: ['allTasks'],
        queryFn: (qk) => { return fetchAllTasks(tasksUrl) },
    })
    const createTaskMutation = useMutation({
        mutationFn: (task) => { return createTask(tasksUrl, task) },
        onSuccess: () => { queryClient.invalidateQueries('allTasks') },
    })
    const updateTaskMutation = useMutation({
        mutationFn: (task) => { return updateTask(tasksUrl, task) },
        onSuccess: () => { queryClient.invalidateQueries('allTasks') },
    })
    const deleteTaskMutation = useMutation({
        mutationFn: (task) => { return deleteTask(tasksUrl, task) },
        onSuccess: () => { queryClient.invalidateQueries('allTasks') },
    })

    if (isLoading) {
        return <span>Loading...</span>
    }

    if (isError) {
        return <span>Error: {error.message}</span>
    }

    const tasks = data || []
    const tasksElement = tasks.map(task => {
        const deadline = task.deadline
        if (deadline) {
            task.deadline = new Date(task.deadline)
        }
        return task
    }).map((task) =>
        <tr key={task.id}>
            <td>{task.id}</td>
            <td>{task.text}</td>
            <td>{task.deadline && task.deadline.toString()}</td>
            <td><button onClick={() => { setUpdatingTask(task) }}>Upd</button></td>
            <td><button onClick={() => { deleteTaskMutation.mutate(task) }}>Del</button></td>
        </tr>
    )
    return (
        <>
            <div>
                <p>Tasks: {tasks.length}</p>
                <table>
                    <thead>
                        <tr>
                            <th>Id</th><th>Text</th><th>Deadline</th>
                        </tr>
                    </thead>
                    <tbody>
                        {tasksElement}
                    </tbody>
                </table>
            </div>
            <div>
                <button onClick={() => {
                    console.log(`open ${isOpenCreateTaskWindow}`)
                    setOpenCreateTaskWindow(true)
                }}>Create Task</button>

                <ReactModal isOpen={isOpenCreateTaskWindow}>
                    <TaskEditor submitCaption="Create" 
                        submitFn={(task) => {
                            console.log(`submit ${JSON.stringify(task)}`)
                            createTaskMutation.mutate(task)
                            setOpenCreateTaskWindow(false)
                        }}
                        cancelFn={() => setOpenCreateTaskWindow(false)}
                    />
                </ReactModal>

                <ReactModal isOpen={updatingTask !== null}>
                    <TaskEditor submitCaption="Update" idReadonly task={updatingTask} submitFn={(task) => {
                        console.log(`submit ${JSON.stringify(task)}`)
                        updateTaskMutation.mutate(task)
                        setUpdatingTask(null)
                    }} cancelFn={() => setUpdatingTask(null)} />
                </ReactModal>

            </div>

        </>
    )
}

async function fetchAllTasks(tasksUrl) {
    console.log(`fetch tasks ${tasksUrl}`);
    return await fetch(tasksUrl, { method: 'GET' })
        .then((response) => response.json())
        .catch((err) => { console.error(err); });
}

async function createTask(tasksUrl, task) {
    console.log(`create task ${task}`);
    return await fetch(tasksUrl, {
        method: 'POST', body: JSON.stringify(task),
        headers: { 'Content-type': 'application/json; charset=UTF-8' },
    })
        .then((response) => {
            return response.ok ? response.json() : response.text().then((errMessage) => {
                throw new Error(`create task error '${response.statusText}', result ${errMessage}`)
            })
        })
        .then((json) => console.log(`create task result: ${JSON.stringify(json)}`))
        .catch((err) => { console.error(err) });
}

async function updateTask(tasksUrl, task) {
    console.log(`update task ${task}`);
    return await fetch(tasksUrl + `/${task.id}`, {
        method: 'PUT', body: JSON.stringify(task),
        headers: { 'Content-type': 'application/json; charset=UTF-8' },
    })
        .then((response) => {
            return response.ok ? response.json() : response.text().then((errMessage) => {
                throw new Error(`update task error '${response.statusText}', result ${errMessage}`)
            })
        })
        .then((json) => console.log(`update task result: ${JSON.stringify(json)}`))
        .catch((err) => { console.error(err) });
}

async function deleteTask(tasksUrl, task) {
    console.log(`delete task ${task}`);
    return await fetch(tasksUrl + `/${task.id}`, {
        method: 'DELETE',
    })
        .then((response) => {
            return response.ok ? response.json() : response.text().then((errMessage) => {
                throw new Error(`delete task error '${response.statusText}', result ${errMessage}`)
            })
        })
        .then((json) => console.log(`delete task result: ${JSON.stringify(json)}`))
        .catch((err) => { console.error(err) });
}