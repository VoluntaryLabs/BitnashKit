//
//  BNJavaInstall.h
//  BitnashKit
//
//  Created by Steve Dekorte on 11/9/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface BNJavaInstall : NSObject

- (NSString *)javaExePath;
- (BOOL)isJavaInstalled;
- (void)presentInstaller;

@end
