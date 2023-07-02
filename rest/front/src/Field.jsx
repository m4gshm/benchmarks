import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import { useContext } from "react";

import { createContext } from 'react';

export const EntityContext = createContext();

export function Field({ id, name, editable = true }) {
    const { value, setValue } = useEntityFieldEdit(id)
    return editable ? (
        <div>
            <label>
                {name}: <input type="text" key={id} id={id} value={value} onChange={(e) => setValue(e.target.value)} />
            </label>
        </div>
    ) : (
        <div>
            <label>
                {name}: {value}
            </label>
        </div>
    )
}

export function DateField({ id, name, editable = true }) {
    const { value, setValue } = useEntityFieldEdit(id)
    return editable ? (
        <div>
            <label>
                {name}: <DatePicker type="text" key={id} id={id} dateFormat="yyyy-MM-dd'T'HH:mm:ss'Z'" showTimeSelect selected={value} onChange={setValue} />
            </label>
        </div>
    ) : (
        <div>
            <label>
                {name}: {value}
            </label>
        </div>
    )
}

function useEntityFieldEdit(field) {
    const { entity, setEntity } = useContext(EntityContext);

    const value = entity[field]

    const setValue = (value) => {
        const updateEntity = { ...entity, [field]: value }
        setEntity(updateEntity)
    }

    return { value, setValue }
}