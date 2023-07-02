import { useState } from "react";
import { Field, DateField, EntityContext } from "./Field";


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
            <EntityContext.Provider value={{ entity: onEditTask, setEntity: setOnEditTask }}>
                <Field id="id" name="Id" editable={!idReadonly} /><br />
                <Field id="text" name="Text" /><br />
                <DateField id="deadline" name="Deadline" /><br />
                <button type="submit">{submitCaption}</button>
                <button onClick={cancelFn}>Cancel</button>
            </EntityContext.Provider>
        </form >
    )
}