//
//  BluetoothInterface.swift
//  AudioCompassController
//
//  Created by James on 13/03/2015.
//  Copyright (c) 2015 James Alvarez. All rights reserved.
//

import IOBluetooth
import IOBluetoothUI

import Foundation

//CB10D561-29E0-4414-821B-32BE93D412D3

class BluetoothInterface : NSObject {
    var delegate : BluetoothController!
    

    var mRFCOMMChannel : IOBluetoothRFCOMMChannel?
    
    override init() {

        super.init()
    }
    
    func connectToServer() -> Bool {
        let deviceSelector = IOBluetoothDeviceSelectorController.deviceSelector()
        
        if (deviceSelector == nil) {
            fatalError("No BT device selector!!")
        }
        
        let serviceUUID = IOBluetoothSDPUUID(bytes: [
            0xcb, 0x10, 0xd5, 0x61, 0x29, 0xe0, 0x44, 0x14,
            0x82, 0x1b, 0x32, 0xbe, 0x93, 0xd4, 0x12, 0xd3
            ] as [UInt8], length: 16)
        
        deviceSelector.addAllowedUUID(serviceUUID)
        
        if (deviceSelector.runModal() != Int32(kIOBluetoothUISuccess)) {
            return false
        }
        
        let deviceArray = deviceSelector.getResults()
        let selectedDevice: AnyObject = deviceArray[0]
        let serviceRecord = selectedDevice.getServiceRecordForUUID(serviceUUID)
    
        var rfcommChannelID : UInt8 = 0
        var status : IOReturn = serviceRecord.getRFCOMMChannelID(&rfcommChannelID)
        
        if (status != kIOReturnSuccess) {
            println("Error: \(status) getting RFCOMM channel ID from service.")
            return false
        }
        
        println("Service selected -\(serviceRecord.getServiceName())- RFCOMM Channel ID = \(rfcommChannelID)")
        
        status = selectedDevice.openConnection()
        if (status != kIOReturnSuccess) {
            println("Error: \(status) opening connection to device")
            return false
        }

        status = selectedDevice.openRFCOMMChannelSync(&mRFCOMMChannel, withChannelID: rfcommChannelID, delegate: self)
        
        if ((status == kIOReturnSuccess) && (mRFCOMMChannel != nil)) {
            return true
        } else {
            println("Error: \(status) - unable to open RFCOMM channel.")
            return false
        }
    }
    
    func disconnectFromServer() {
        if (mRFCOMMChannel != nil) {
            var device = mRFCOMMChannel!.getDevice()
            mRFCOMMChannel!.closeChannel()
            mRFCOMMChannel = nil
            device.closeConnection()
        }
    }
    
    func remoteDeviceName() -> String? {
        if let mrf = mRFCOMMChannel {
            var device = mrf.getDevice()
            return device.name
        }
        return nil
    }
    
    func sendData(buffer : UnsafeMutablePointer<Void>, length : UInt16) -> Bool {
        if mRFCOMMChannel == nil { return false}
        var numBytesRemaining : UInt16
        var result : IOReturn
        var rfcommChannelMTU : BluetoothRFCOMMMTU
        
        numBytesRemaining = length
        result = kIOReturnSuccess
        
        rfcommChannelMTU = mRFCOMMChannel!.getMTU()
        
        while((result == kIOReturnSuccess)&&(numBytesRemaining > 0)) {
            var numBytesToSend : UInt16
            if (numBytesRemaining > rfcommChannelMTU){
                numBytesToSend = rfcommChannelMTU
            } else {
                numBytesToSend = numBytesRemaining
            }

            result = mRFCOMMChannel!.writeSync(buffer, length: numBytesToSend)
            numBytesRemaining -= numBytesToSend
            buffer.advancedBy(Int(numBytesToSend))
        }
        
        return ((numBytesRemaining == 0) && (result == kIOReturnSuccess))
    }
    
    func rfcommChannelData(rfcommChannel : IOBluetoothRFCOMMChannel, dataPointer : UnsafePointer<Void>, dataLength : UInt32) {
        delegate.handleNewData(NSData(bytes: dataPointer, length: Int(dataLength)))
    }
    
    func rfcommChannelClosed(rfcommChannel : IOBluetoothRFCOMMChannel) {
        if delegate != nil {
            delegate.handleRemoteDisconnection()
        }
    }
    
    func localDeviceName() -> String? {
        /*var ldn : BluetoothDeviceName
        if (IOBluetoothLocalDeviceReadName(ldn,nil,nil,nil) = kIOReturnSuccess) {
            return String(ldn)
        }*/
        return nil
    }
    
    func registerDelegate(controller : BluetoothController?) {
        delegate = controller
    }
}