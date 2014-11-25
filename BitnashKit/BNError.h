//
//  BNError.h
//  BitnashKit
//
//  Created by Rich Collins on 4/16/14.
//  Copyright (c) 2014 voluntary.net. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BNObject.h"

@interface BNError : BNObject

@property NSString *description;
@property NSNumber *insufficientValue;

- (BOOL)isInvalidAddress;

@end
