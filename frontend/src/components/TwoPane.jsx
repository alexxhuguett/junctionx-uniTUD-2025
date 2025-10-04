import "../stylesheets/twoPane.css"

export default function TwoPane({ left, right }) {
  return (
    <div className="two-pane">
      <section className="pane pane-left">{left}</section>
      <section className="pane pane-right">{right}</section>
    </div>
  );
}
