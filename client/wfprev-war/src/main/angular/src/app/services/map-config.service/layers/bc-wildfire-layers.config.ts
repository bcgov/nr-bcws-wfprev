import { LayerSettings } from "src/app/components/models";
import { FirePerimetersLayerConfig } from "./bc-wildfire-layers/fire-perimeters.config";
import { OutOfControlWildfiresLayerConfig } from "./bc-wildfire-layers/out-of-control-wildfires.config";
import { BeingHeldWildfiresLayerConfig } from "./bc-wildfire-layers/being-held-wildfires.config";
import { UnderControlWildfiresLayerConfig } from "./bc-wildfire-layers/under-control-wildfires.config";
import { OutWildfiresLayerConfig } from "./bc-wildfire-layers/out-wildfires.config";
import { WuiRiskClassLayerConfig } from "./bc-wildfire-layers/wui-risk-class.config";
import { OneKMWUILayerConfig } from "./bc-wildfire-layers/1-km-wui.config";
import { HumanInterfaceBufferLayerConfig } from "./bc-wildfire-layers/human-interface-buffer.config";

export function BcWildfireLayersConfig(ls: LayerSettings, authHeader?: Record<string, string>) {
  const items = [
    FirePerimetersLayerConfig(ls, authHeader),
    OutOfControlWildfiresLayerConfig(ls),
    BeingHeldWildfiresLayerConfig(ls),
    UnderControlWildfiresLayerConfig(ls),
    OutWildfiresLayerConfig(ls),
    WuiRiskClassLayerConfig(ls),
    OneKMWUILayerConfig(ls),
    HumanInterfaceBufferLayerConfig(ls),
  ];
  return {
    id: "bc-wildfire-layers",
    type: "folder",
    title: "BC Wildfire Layers",
    isVisible: false,
    isExpanded: false,
    items,
    layers: items,
  };
}
