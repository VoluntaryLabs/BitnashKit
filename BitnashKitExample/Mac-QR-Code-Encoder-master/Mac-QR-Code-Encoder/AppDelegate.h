//
//  AppDelegate.h
//  Mac-QR-Code-Encoder
//
//  Created by John Slaughter on 12/10/12.
//  Copyright (c) 2012 John Slaughter. All rights reserved.
//

#import <Cocoa/Cocoa.h>

@interface AppDelegate : NSObject <NSApplicationDelegate> {
    NSTextField *stringField;
}

@property (assign) IBOutlet NSImageView *imageView;
@property (assign) IBOutlet NSWindow *window;
@property (assign) IBOutlet NSTextField *stringField;

- (IBAction)generateQR:(id)sender;

@end