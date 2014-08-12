//
//  BNPriceFormatter.m
//  BitnashKit
//
//  Created by Steve Dekorte on 8/12/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNPriceFormatter.h"

@implementation BNPriceFormatter

- (id)init
{
    self = [super init];
    
    if (self)
    {
        //[self setLocalizesFormat:NO];
        [self setNumberStyle:NSNumberFormatterDecimalStyle];
        [self setPartialStringValidationEnabled:YES];
        [self setMinimum:0];
        [self setMaximumFractionDigits:6];
        [self setMaximumIntegerDigits:3];
    }

    return self;
}

@end
