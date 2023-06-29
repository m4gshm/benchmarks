import React, { useState } from "react";
import { Field, DateField } from "./Field";


export default function Task({ buttonCaption = "Update", task = {}, handlerFn }) {
    const handleSubmit = (e) => {
        e.preventDefault();
        const changedTask = {
            ...task,
            id: e.target.id.value,
            text: e.target.text.value,
            deadline: e.target.deadline.value,
        }
        if (handlerFn) {
            handlerFn(changedTask)
        } else {
            console.log(`no handler function for task ${JSON.stringify(changedTask)}`)
        }
    };

    return (
        <form onSubmit={handleSubmit}>
            <Field id="id" name="Id" value={task.id} /><br />
            <Field id="text" name="Text" value={task.text} /><br />
            <DateField id="deadline" name="Deadline" value={task.deadline} /><br />
            <button type="submit">{buttonCaption}</button>
        </form>
    )
}