//
//  AppDelegate.m
//  Mac-QR-Code-Encoder
//
//  Created by John Slaughter on 12/10/12.
//  Copyright (c) 2012 John Slaughter. All rights reserved.
//

#import "AppDelegate.h"
#import "QRCodeGenerator.h"

@implementation AppDelegate
@synthesize stringField;

@synthesize imageView;

- (void)dealloc
{
    [super dealloc];
}

- (void)applicationDidFinishLaunching:(NSNotification *)aNotification
{
    // Insert code here to initialize your application
    
    
    
    
}

- (IBAction)generateQR:(id)sender {
    
    imageView.image = [QRCodeGenerator qrImageForString:[stringField stringValue] imageSize:imageView.bounds.size.width];
    
}
@end