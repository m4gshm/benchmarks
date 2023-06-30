import { useState } from "react";
import { Field, DateField } from "./Field";


export default function TaskEditor({ submitCaption = "OK", task = {}, idReadonly = false, submitFn, cancelFn = () => { } }) {
    const [onEditTask, setOnEditTask] = useState(task || {})
    const handleSubmit = (e) => {
        e.preventDefault();
        if (submitFn) {
            submitFn(onEditTask)
        } else {
            console.log(`no handler function for task ${JSON.stringify(onEditTask)}`)
        }
    };

    return (
        <form onSubmit={handleSubmit}>
            <Field id="id" name="Id" editable={!idReadonly} value={onEditTask.id} setValue={v => setOnEditTask({ ...onEditTask, id: v })} /><br />
            <Field id="text" name="Text" value={onEditTask.text} setValue={v => setOnEditTask({ ...onEditTask, text: v })} /><br />
            <DateField
                id="deadline"
                name="Deadline"
                value={onEditTask.deadline}
                setValue={v => setOnEditTask({ ...onEditTask, deadline: v })}
            /><br />
            <button type="submit">{submitCaption}</button>
            <button onClick={cancelFn}>Cancel</button>
        </form>
    )
}