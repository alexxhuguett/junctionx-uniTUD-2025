import "../../stylesheets/alert.css"

export default function BreakAlert() {
    return (
        <div class="alert" role="alert">
            <div>Do you want to take a break?</div>
            <button type="button" class="close" data-dismiss="alert" aria-label="Close">Close</button>
            <button type="button" class="accept" data-dismiss="alert" aria-label="Accept">Accept</button>
        </div>
    )
}