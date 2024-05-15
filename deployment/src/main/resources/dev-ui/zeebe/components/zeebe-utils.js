import { html} from 'lit';
import '@vaadin/icon';

export function diagramId(diagram, id) {
    return html`
            <vaadin-icon icon="font-awesome-solid:location-dot" style="cursor: pointer; color: var(--quarkus-blue);"
                         @mouseover=${() => diagram.value.addMarker(id)} 
                         @mouseout=${() => diagram.value.removeMarker(id)}
            ></vaadin-icon>
        `;
}