//
//  Controller.swift
//  AudioCompassController
//
//  Created by James on 14/03/2015.
//  Copyright (c) 2015 James Alvarez. All rights reserved.
//

import Foundation
import Cocoa

class BluetoothController : NSObject, NSTextFieldDelegate {
    
    @IBOutlet var connectButton : NSButton!
    @IBOutlet var disconnectButton : NSButton!
    @IBOutlet var saveButton : NSButton!
    @IBOutlet var parameterPopup : NSPopUpButton!
    @IBOutlet var statusLabel : NSTextField!
    
    @IBOutlet var parameter1 : NSTextField!
    @IBOutlet var parameter2 : NSTextField!
    @IBOutlet var parameter3 : NSTextField!
    @IBOutlet var parameter4 : NSTextField!
    @IBOutlet var parameter5 : NSTextField!
    @IBOutlet var parameter6 : NSTextField!
    @IBOutlet var parameter7 : NSTextField!
    @IBOutlet var parameter8 : NSTextField!
    @IBOutlet var parameter9 : NSTextField!
    @IBOutlet var parameter10 : NSTextField!
    @IBOutlet var parameter11 : NSTextField!
    @IBOutlet var parameter12 : NSTextField!


    var parameters : [NSTextField]!
    var selectedParametersName : String = "Default"
    var parameterNames : [String] = ["Default"]
    var bluetoothInterface : BluetoothInterface!
    var connected : Bool = false
    var localDeviceName : String!
    
    override init() {
        bluetoothInterface = BluetoothInterface()
        super.init()
        bluetoothInterface.registerDelegate(self)
        localDeviceName = bluetoothInterface.localDeviceName()
        
    }
    
    override func awakeFromNib() {
        parameters = [parameter1,
            parameter2,
            parameter3,
            parameter4,
            parameter5,
            parameter6,
            parameter7 ,
            parameter8 ,
            parameter9 ,
            parameter10,
            parameter11,
            parameter12]
        
        var array = NSUserDefaults.standardUserDefaults().arrayForKey("SavedNames")
        if let arr = array {
            for a in arr {
                if let s = a as? String {
                    parameterNames.append(s)
                }
            }
        }
        
        for p in parameters {
            let f = p.formatter as! NSNumberFormatter
            f.usesGroupingSeparator = false
        }
        parameterPopup.addItemsWithTitles(parameterNames)
        parameterPopup.selectItemWithTitle(selectedParametersName)
        loadParameters(selectedParametersName)
    }
    
    
    func handleRemoteDisconnection() {
        bluetoothInterface.disconnectFromServer()
        bluetoothInterface.registerDelegate(nil)
        connectButton.state = 0
        connectButton.title = "Connect"
        connected = false
        statusLabel.stringValue = "Disconnected"
    }
    
    func newData() {
    }
    
    
    func windowShouldClose(sender : NSWindow) -> Bool {
        return true
    }
    
    @IBAction func newButtonPress(sender : AnyObject) {
        
        var alert = NSAlert()
        alert.messageText  = "Enter new name:"
        var input = NSTextField(frame: NSMakeRect(0,0,200,24))
        alert.accessoryView = input
        var button = alert.runModal()
        if (input.stringValue != "") {
            addParameterSet(input.stringValue)
        }
    }
    
    func addParameterSet(name : String) {
        parameterNames.append(name)
        NSUserDefaults.standardUserDefaults().setObject(parameterNames, forKey: "SavedNames")
        parameterPopup.removeAllItems()
        parameterPopup.addItemsWithTitles(parameterNames)
        parameterPopup.selectItemWithTitle(name)
        loadParameters("Default")
        saveParameters(name)
    }
    
    


    @IBAction func connectButtonPress(sender : AnyObject) {
        
        if !connected {
            if bluetoothInterface.connectToServer() == false {
                self.handleRemoteDisconnection()
            } else {
                connectButton.state =  1
                connectButton.title = "Connected"
                connected = true
                statusLabel.stringValue = "Connected"
            }
        } else {
            self.handleRemoteDisconnection()
        }
    }
    
    @IBAction func selectParameterPopup(sender : AnyObject) {
        if let item = parameterPopup.selectedItem {
            selectedParametersName = item.title
            loadParameters(item.title)
            
            statusLabel.stringValue = "Loaded"
        }
    }
    
    func loadParameters(name : String) {
        
        if name == "Default" {
            parameter1.floatValue = 13
            parameter2.floatValue = 30
            parameter3.floatValue = 0.5
            parameter4.floatValue = 0.35
            parameter5.floatValue = 440
            parameter6.floatValue = 880
            parameter7.floatValue = 1
            parameter8.floatValue = 1
            parameter9.floatValue = 880
            parameter10.floatValue = 880
            parameter11.floatValue = 0.0
            parameter12.floatValue = 1
        } else {
            var ud = NSUserDefaults.standardUserDefaults()
            var key : String
            for (index,p) in enumerate(parameters) {
                key = name + String(index)
                p.floatValue = ud.floatForKey(key)
            }
        }
    }
    
    
    
    override func controlTextDidEndEditing(obj: NSNotification) {
        if selectedParametersName != "Default" {
            saveParameters(selectedParametersName)
        } else {
            loadParameters("Default")
        }
    }

    
    func saveParameters(name : String) {
        var ud = NSUserDefaults.standardUserDefaults()
        var key : String
        for (index,p) in enumerate(parameters) {
            key = name + String(index)
            ud.setFloat(p.floatValue, forKey: key)
        }
    }
    
    @IBAction func sendButtonPress(sender : AnyObject) {
        var string : NSMutableString = ""
        for p in parameters {
            string.appendString(p.stringValue)
            string.appendString(",")
        }
        
        var voidPtr: UnsafeMutablePointer<Void> = unsafeBitCast(string.UTF8String, UnsafeMutablePointer<Void>.self)
        bluetoothInterface.sendData(voidPtr, length: UInt16(string.length))
    }

    
    func handleNewData(dataObject : NSData) {
        
    }

    
}