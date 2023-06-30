import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import { useState } from "react";


export function Field({ id, name, value, setValue, editable = true }) {
    return editable ? (
        <div>
            <label>
                {name}: <input type="text" key={id} id={id} value={value} onChange={(e) => { setValue && setValue(e.target.value) }} />
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

export function DateField({ id, name, value, setValue, editable = true }) {
    let stateValue
    let setStateValue
    if (setValue === undefined) {
        [stateValue, setStateValue] = useState();
    } else {
        stateValue = value
        setStateValue = setValue
    }
    return editable ? (
        <div>
            <label>
                {name}: <DatePicker type="text" key={id} id={id} dateFormat="yyyy-MM-dd'T'HH:mm:ss'Z'" showTimeSelect selected={stateValue} onChange={setStateValue} />
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