declare module 'react-native-ble' {

    export function startMonitoringRegion():void;
    export function stopMonitoringRegion():void;
    export function startRangingInRegion():void;
    export function stopRangingInRegion():void;
    export function initializeBeaconManager():void;
}