import "../../stylesheets/alert.css"

export default function Alert({message}) {
    return (
        <div class="alert" role="alert">{message}</div>
    )
}