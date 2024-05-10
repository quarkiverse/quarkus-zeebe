import { LitElement, html, css} from 'lit';
import {} from "./bpmn-navigated-viewer.development.js";
import '@vaadin/icon';
import {ref, createRef} from 'lit/directives/ref.js';

export class ZeebeBpmnDiagram extends LitElement {

    static styles = css`

        .relative {
            position: relative;
        }

        .bpmn-element-active .djs-visual > :nth-child(1) {
            stroke: rgb(132 204 22) !important;
        }

        .bjs-breadcrumbs {
            display: none;
        }

        .bpmn-element-incident .djs-visual * {
            stroke: rgba(255, 0, 0, 1) !important;
            fill: rgba(255, 0, 0, 0.1) !important;
            /*stroke-width: 1px !important;*/
        }

        .bpmn-element-completed .djs-visual * {
            stroke: rgb(132 204 22) !important;
            stroke-width: 2px !important;
            fill: rgb(190 242 100) !important;
        }

        .bpmn-element-selected .djs-visual > :nth-child(1) {
            stroke: rgb(59 130 246) !important;
            stroke-dasharray: 5;
            stroke-width: 2px !important;
        }

        .bpmn-info {
            background-color: rgba(0, 123, 255, 255);
            color: White;
            border-radius: 5px;
            font-size: 12px;
            padding: 5px;
            min-height: 16px;
            width: 100px;
            text-align: center;
        }

        .bpmn-diagram {
            height: 550px;
            width: 100%
        }
        .diagram_e {
            display: inline-block;
            white-space: nowrap;
            border-radius: 9999px;
            background-color: rgb(163 230 53);
            text-align: center;
            padding: 0.25rem 0.5rem;
            vertical-align: baseline;
            font-size: 0.75em;
            font-weight: 700;
            line-height: 1;
            color: rgb(255 255 255);
        }
        .diagram_e_active {
            background-color: rgb(163 230 53);
        }

        .diagram_e_completed {
            background-color: rgb(156 163 175);
        }
    `;

    _containerRef = createRef();

    static properties = {
        _xml: { state: true },
        _data: { state: true },
        _viewer: {state: true},
    };

    connectedCallback() {
        super.connectedCallback();

    }

    set data(val) {
        this._data = val;
    }

    set xml(val) {
        this._xml = val;
    }

    firstUpdated() {
        this._viewer = new BpmnJS({container: this._containerRef.value, width: '100%', height: '100%'});
        this._viewer.on('import.done', event => this._afterXmlImport(event));

        this._renderDiagram();
    }

    updated(changedProperties) {
        if (changedProperties.has('_data')) {
            this._renderDiagram();
        }
    }

    _afterXmlImport() {
        if (this._data) {
            if (this._data.elements) {

                const overlays = this._viewer.get('overlays');

                Object.entries(this._data.elements).forEach(([key, value]) => {
                    if (!value.ELEMENT_ACTIVATED) {
                        value.ELEMENT_ACTIVATED = 0;
                    }
                    if (!value.ELEMENT_COMPLETED) {
                        value.ELEMENT_COMPLETED = 0;
                    }
                    const active = (value.ELEMENT_ACTIVATED - value.ELEMENT_COMPLETED);
                    const status = ((active > 0) ? 'diagram_e_active' : 'diagram_e_completed');

                    try {
                        overlays.add(key, {
                            id: key,
                            position: {top: -27, left: 0},
                            html: '<span class="diagram_e ' + status + '">' + active + ' | ' + value.ELEMENT_COMPLETED + ' </span>'
                        });
                    } catch (err) {
                        // console.log(err);
                    }
                });
            }
        }
    }

    async _renderDiagram() {
        try {
            const result = await this._viewer.importXML(this._xml);
            const { warnings } = result;
            if (warnings.length > 0) {
                console.log("Diagram warnings " + warnings);
            }
        } catch (err) {
            console.log("Diagram rendering: " + err);
        }
    }

    render() {
        return html`
            <div class="relative">
                <div id="zeebe-diagram" class="bpmn-diagram" ${ref(this._containerRef)}></div>
                <vaadin-icon  @click=${() => this._resetView()} style="position: absolute; top: 0.625rem; right: 0.625rem; width: 2.25rem; height: 2.25rem;" icon='font-awesome-solid:location-crosshairs'></vaadin-icon>
            </div>
        `;
    }

    _resetView() {
        this._viewer.get('canvas').zoom('fit-viewport')
    }
}

customElements.define('zeebe-bpmn-diagram', ZeebeBpmnDiagram);