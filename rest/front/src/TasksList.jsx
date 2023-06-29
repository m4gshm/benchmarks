import { useEffect, useState } from "react"

import {
    useQuery,
    useMutation,
    useQueryClient,
    QueryClient,
    QueryClientProvider,
} from 'react-query';
import Task from "./Task"

export default function TasksList() {
    const baseUrl = env.BACKEND_URL
    const tasksUrl = baseUrl + "/" + "task"

    const queryClient = useQueryClient();

    const { isLoading, isError, data, error } = useQuery({
        queryKey: ['allTasks'],
        queryFn: (qk) => { return fetchAllTasks(tasksUrl) },
    })
    const mutation = useMutation({
        mutationFn: (task) => { return createTask(tasksUrl, task) },
        onSuccess: () => {
            queryClient.invalidateQueries('allTasks');
        },
    })

    if (isLoading) {
        return <span>Loading...</span>
    }

    if (isError) {
        return <span>Error: {error.message}</span>
    }

    const tasks = data || []
    const tasksElement = tasks.map((task) =>
        <tr key={task.id}>
            <td>{task.id}</td>
            <td>{task.text}</td>
            <td>{task.deadline}</td>
        </tr>
    )

    const createTaskHandler = function (task) {
        console.log(`submit ${JSON.stringify(task)}`)
        mutation.mutate(task)
    }

    const createTaskElement = (<Task buttonCaption="Create" handlerFn={createTaskHandler} />)

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
                Create task:
            </div>
            <div>
                {createTaskElement}
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
