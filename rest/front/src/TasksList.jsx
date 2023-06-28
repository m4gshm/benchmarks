import { useEffect, useState } from "react"
import Task from "./Task"

export default function TasksList() {
    const tasksUrl = env.BACKEND_URL + "/" + "task"


    const [tasks, setTasks] = useState([]);

    useEffect(() => {
        console.log(`fetch tasks ${tasksUrl}`)
        fetch(tasksUrl, { method: 'GET' })
            .then((response) => response.json())
            .then((json) => setTasks(json))
            .catch((err) => { console.error(err) });
    }, [])

    const content = tasks.map((task) =>
        <tr>
            <td>{task.id}</td>
            <td>{task.text}</td>
            <td>{task.deadline}</td>
        </tr>
    )

    const createTask = async function (task) {
        fetch(tasksUrl, {
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

    return (
        <>
            <div>
                <p>Tasks: {tasks.length}</p>
                <table>
                    <tr>
                        <th>Id</th><th>Text</th><th>Deadline</th>
                    </tr>
                    {content}
                </table>
            </div>
            <div>
                Create task:
            </div>
            <Task buttonCaption="Create" setTask={createTask} />
        </>
    )
}
