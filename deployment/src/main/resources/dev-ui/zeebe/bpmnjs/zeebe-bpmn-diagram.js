import { LitElement, html, css} from 'lit';
import {} from "./bpmn-navigated-viewer.development.js";
import '@vaadin/icon';
import '@vaadin/tooltip';
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

        .bpmn-element-incident .djs-visual > text > tspan {
            stroke: rgba(255, 0, 0, 1) !important;
            stroke-width: 0 !important;
            fill: rgba(255, 0, 0, 1) !important;
        }
        
        .bpmn-element-completed .djs-visual * {
            stroke: rgb(132 204 22) !important;
            stroke-width: 2px !important;
            fill: rgb(190 242 100) !important;
        }

        .bpmn-element-completed .djs-visual > text > tspan {
            stroke: black !important;
            stroke-width: 0 !important;
            fill: black !important;
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
        
        .element-tooltip {
            white-space: nowrap;
            padding: 0.5rem;
            background-color: rgb(255 255 255);
            border-radius: 0.5rem;
            border-color: rgb(229 231 235);
            box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
            z-index: 10;
            outline: 2px solid transparent;
            outline-offset: 2px;
            font-size: 0.875rem; /* 14px */
            line-height: 1.25rem; /* 20px */
            font-weight: 300;
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
        if (!this._data) {
            return;
        }

        if (this._data.elements) {
            this._addProcessMarkers();
            return;
        }
        this._addMarkers();
        this._addToolTip();
    }

    _addToolTip() {
        if (!this._data.bpmnElementInfos) {
            return;
        }

        const eventBus = this._viewer.get("eventBus");
        let infoOverlayId;
        const overlays = this._viewer.get("overlays");

        let bpmnElementInfo = {};
        this._data.bpmnElementInfos.forEach(e => bpmnElementInfo[e.elementId] = e.info);

        eventBus.on("element.hover", function(e) {
            let elementId = e.element.id;
            let info = bpmnElementInfo[elementId];
            if (info) {
                infoOverlayId = overlays.add(elementId, {
                    position: { bottom: -5, left: 0 },
                    html: '<div role="tooltip" data-tooltip="true" class="element-tooltip">' + info + '</div>'
                });
            }
        });

        eventBus.on("element.out", function(e) {
            if (infoOverlayId) {
                overlays.remove(infoOverlayId);
            }
        });

    }

    _addProcessMarkers() {

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

    _addMarkers() {

        const canvas = this._viewer.get('canvas');

        if (this._data.activeActivities) {
            this._data.activeActivities.forEach(e => canvas.addMarker(e, "bpmn-element-active"));
        }

        if (this._data.incidentActivities) {
            this._data.incidentActivities.forEach(e => canvas.addMarker(e, 'bpmn-element-incident'));
        }

        if (this._data.incidentActivities) {
            console.log(this._data.incidentActivities);
            const overlays = this._viewer.get('overlays');
            let icon = `
                <span title="incident">
                    <svg fill="white" style="width: 2rem; height: 2rem;" stroke="red" stroke-width="2" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z"></path>
                    </svg>
                </span>
            `
            this._data.incidentActivities.forEach(e => {
                    overlays.add(e, {
                        position: { top: -30, right: 20 },
                        html: icon
                    } );
            });
        }

        const injector = this._viewer.get('injector');
        const elementRegistry = injector.get('elementRegistry');
        const graphicsFactory = injector.get('graphicsFactory');
        if (this._data.takenSequenceFlows) {
            this._data.takenSequenceFlows.forEach(e => {
                const element = elementRegistry.get(e);
                const gfx = elementRegistry.getGraphics(element);
                const color = 'rgb(132 204 22)';
                const di = element && element.di;
                di.set('stroke', color);
                di.set('fill', color);
                graphicsFactory.update('connection', element, gfx);
            });
        }

        if (this._data.completedActivities) {
            this._data.completedActivities.forEach(e => canvas.addMarker(e, 'bpmn-element-completed'));
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