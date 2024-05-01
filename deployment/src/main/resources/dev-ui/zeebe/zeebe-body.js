import { LitElement, html, css} from 'lit';
import './zeebe-processes.js';
import './zeebe-instances.js';

export class ZeebeBody extends LitElement {

    static styles = css`
    `;

    static properties = {
        nav: {state: true},
        extension: {state: true},
    };

    render() {
        switch (this.nav) {
            case 'processes':
                return html`<zeebe-processes .extension=${this.extension}></zeebe-processes>`;
            case 'instances':
                return html`<zeebe-instances .extension=${this.extension}></zeebe-instances>`;
        }
        return html`<p>Not defined</p>`
    }

}

customElements.define('zeebe-body', ZeebeBody);